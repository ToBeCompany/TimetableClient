package com.example.timetable.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.example.timetable.App
import com.example.timetable.MainActivity
import com.example.timetable.R
import com.example.timetable.data.Route
import com.example.timetable.worker.BusStopsBottomSheet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect


class MapsFragment : Fragment()
{
    private val PERMISSION_CODE = 200
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(App.globalContext)
    }
    private var cancellationTokenSource = CancellationTokenSource()

    private var busMarker: Marker? = null

    private val args: MapsFragmentArgs by navArgs()

    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(
            (activity?.application as App).database
                .routeDao()
        )
    }

    lateinit var googleMap: GoogleMap

    var flight/*: Flight*/: Route? = null

    lateinit var findBusButton: ImageButton
    lateinit var myLocationButton: ImageButton

    private val callback = OnMapReadyCallback { google_map ->
        googleMap = google_map // ассинхронный вызов - в другом потоке

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.getFlight(args.id).also {
                if (it != null) {
                    flight = it
                    (requireActivity() as MainActivity).setActionBarTitle(flight?.name)
                    mapReady()
                    Log.d("response_server", "data (flight) Ready")
                } else
                    Log.d("response_server", "data (flight) is null")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_maps, container, false)

        findBusButton = root.findViewById(R.id.findBus_fragment_map)
        myLocationButton = root.findViewById(R.id.myLocationBtn_fragment_map7)
        findBusButton.setOnClickListener {
            if (busMarker == null)
                Snackbar.make(requireView(), getString(R.string.bus_not_connected), Snackbar.LENGTH_LONG).show()
            else
                moveCamera(busMarker?.position)
            Log.d("findbusbtn", busMarker.toString())
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private fun mapReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        val route = flight/*.route*/

        googleMap.uiSettings.isMyLocationButtonEnabled = false

        if (!route?.id.isNullOrEmpty())
            startListeningTracker(route!!.id) // включаю вебсокет]

        val polylineOptions = PolylineOptions() // это будет маршрут (ломаная линия)
        polylineOptions.color(requireContext().getColor(R.color.polyline))
        moveCamera(route!!.points[0].toLatLng())// перемещаем камеру на первую остановку

        route.points.forEach { polylineOptions.add(it.toLatLng()) } // добавляем точки для линии
        val polyline = googleMap.addPolyline(polylineOptions) // добавляем линию (маршрут) на карту

        val busStops = route.busStopsWithTime
        for (i in busStops.indices) // добавляем маркеры на карту
        {
            var marker = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            busStops[i].busStop?.point!!.latitude,
                            busStops[i].busStop?.point!!.longitude
                        )
                    )
                    .title(busStops[i].busStop?.name)
            )
                ?.setTag(i) // в тэг сохраняем индекс данных, потом по этому индексу будем находить даннные в массиве (ти-па привязки данных к маркеру)
        }

        googleMap.setOnMarkerClickListener { marker -> // при нажатии на маркер
            if (marker.tag != null)
                BusStopsBottomSheet(marker.tag as Int, busStops)
                    .show(requireFragmentManager(), "BottomSheetDialog")
            true
        }

        myLocationButton.setOnClickListener {
            checkLocationPermissions()
        }


    }

    private fun startListeningTracker(trackerId: String)
    {
        Log.d("startListeningTracker", "id = $trackerId")
        val busMarkerIcon: BitmapDescriptor = getMarkerIconFromDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bus_marker,
                null
            )!! // создаем и конвертируем Drawable к BitmapDescriptor
        )

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startWebSocket(trackerId).collect {
                Log.d("tracker new pos", it.toString())

                val busPosition = it.toLatLng()
                if (busMarker == null)
                    busMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(busPosition)
                            .title(flight?.name)
                            .icon(busMarkerIcon)
                    )
                else
                    busMarker?.position = busPosition

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            checkLocationPermissions()
        else
            Snackbar.make(requireView(), getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkLocationPermissions()
    {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE
                )
            } else
            {
                if (!googleMap.isMyLocationEnabled) googleMap.isMyLocationEnabled = true
                val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )

                currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                    if (task.isSuccessful)
                    {
                        val location: Location = task.result
                        moveCamera(LatLng(location.latitude, location.longitude))
                    } else
                        "Location (failure): ${task.exception}"
                }
            }
        else
        {
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun moveCamera(point: LatLng?) {
        if (point != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 13.5f), 1500, null)
    }

    override fun onStop() {
        viewModel.stopWebSocket()
        super.onStop()
    }

    override fun onResume() {
        if (flight != null) startListeningTracker(flight!!.id)
        super.onResume()
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor
    {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}