package com.dru128.timetable.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.dru128.timetable.App
import com.dru128.timetable.MainActivity
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.GeoPosition
import dru128.timetable.R
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.worker.BusStopsBottomSheet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import com.dru128.timetable.tools.ProgressManager
import dru128.timetable.databinding.FragmentMapsBinding


class MapsFragment : Fragment()
{
    private lateinit var binding: FragmentMapsBinding
    private lateinit var progressManager: ProgressManager
    private val PERMISSION_CODE = 200
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(App.globalContext)
    }
    private var cancellationTokenSource = CancellationTokenSource()
    private var isTracking = false
    private var busMarker: Marker? = null

    private val viewModel: MapViewModel by viewModels()

    lateinit var googleMap: GoogleMap


    private val args: MapsFragmentArgs by navArgs()
    var route/*: Flight*/: Route? = null

    private val callback = OnMapReadyCallback { google_map ->
        googleMap = google_map // ассинхронный вызов - в другом потоке
        route = Storage.routes[args.id]
        if (route != null) {
            Log.d("data", "(flight) is not null (SUCCESS)")

            (requireActivity() as MainActivity).setActionBarTitle(route?.name)
            route?.positions?.get(0)
                ?.let { tpCamera(geoPosToLatLng(it)) } // перемещаем камеру на первую остановку
            dataReady()
        } else
            Log.d("data", "(flight) is null (FAIL)")

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapsBinding.inflate(inflater)

        progressManager = ProgressManager(binding.parentMapsFragment, requireActivity())
        progressManager.start()

        binding.findBusButton.setOnClickListener {
            if (busMarker == null)
                Snackbar.make(requireView(), getString(R.string.bus_not_connected), Snackbar.LENGTH_LONG).show()
            else
                moveCamera(busMarker?.position)
            Log.d("findbusbtn", busMarker.toString())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private fun dataReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        progressManager.finish()

        googleMap.uiSettings.isMyLocationButtonEnabled = false

        if (!route?.id.isNullOrEmpty())
            startListeningTracker(route!!.id) // включаю вебсокет

        val polylineOptions = PolylineOptions() // это будет маршрут (ломаная линия)
        polylineOptions.color(
            ResourcesCompat.getColor(
                requireContext().resources,
                R.color.polyline,
                null
            )
        )

        route?.positions?.forEach { polylineOptions.add(geoPosToLatLng(it)) } // добавляем точки для линии
        val polyline = googleMap.addPolyline(polylineOptions) // добавляем линию (маршрут) на карту

        val busStops = route!!.busStopsWithTime
        for (i in busStops.indices) // добавляем маркеры на карту
        {
            var marker = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            busStops[i].busStop?.position!!.latitude,
                            busStops[i].busStop?.position!!.longitude
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

        binding.myLocationButton.setOnClickListener {
            checkLocationPermissions()
        }

        val connectivityManager =
            getSystemService(requireContext(), ConnectivityManager::class.java)
        connectivityManager!!.registerDefaultNetworkCallback(networkCallback)
    }

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // сеть доступна для использования
        override fun onAvailable(network: Network) {
            startListeningTracker(route!!.id)
            Log.d("network", "is on")
            super.onAvailable(network)
        }

        // соединение прервано
        override fun onLost(network: Network) {
            stopListeningTracker()
            Log.d("network", "is off")
            super.onLost(network)
        }
    }

    private fun startListeningTracker(trackerId: String) {
        Log.d("Tracker", "startListening, id = $trackerId")
        if (isTracking) return
        isTracking = true

        val busMarkerIcon: BitmapDescriptor = DrawableConvertor().drawableToBitmapDescriptor(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bus_marker,
                null
            )!! // создаем и конвертируем Drawable к BitmapDescriptor
        )

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startWebSocket(trackerId).collect {
                Log.d("Tracker", "new pos ${it.toString()}")

                val busPosition = geoPosToLatLng(it)
                if (busMarker == null)
                    busMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(busPosition)
                            .title(route?.name)
                            .icon(busMarkerIcon)
                    )
                else
                    busMarker?.position = busPosition

            }
        }
    }

    private fun stopListeningTracker() {
        Log.d("Tracker", "stopListening")
        isTracking = false
        viewModel.stopWebSocket()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            checkLocationPermissions()
        else
            Snackbar.make(
                requireView(),
                getString(R.string.permission_not_granted),
                Snackbar.LENGTH_LONG
            ).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkLocationPermissions() {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_CODE
                )
            } else {
                if (!googleMap.isMyLocationEnabled) googleMap.isMyLocationEnabled = true
                val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )

                currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                    if (task.isSuccessful) {
                        val location: Location = task.result
                        moveCamera(LatLng(location.latitude, location.longitude))
                    } else
                        "Location (failure): ${task.exception}"
                }
            }
        else {
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun moveCamera(point: LatLng?) {
        if (point != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 13.5f), 1500, null)
    }

    private fun tpCamera(point: LatLng?) {
        if (point != null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 13.5f))
    }

    override fun onStop() {
        stopListeningTracker()
        super.onStop()
    }

    override fun onStart() {
        if (route != null) startListeningTracker(route!!.id)
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onDestroy()
    }

    private fun geoPosToLatLng(geoPosition: GeoPosition): LatLng =
        LatLng(geoPosition.latitude, geoPosition.longitude)
}