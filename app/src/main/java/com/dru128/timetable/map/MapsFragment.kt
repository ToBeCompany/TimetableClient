package com.dru128.timetable.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.dru128.timetable.MainActivity
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.worker.BusStopsBottomSheet
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.Cancelable
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import dru128.timetable.R
import dru128.timetable.databinding.FragmentMapsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


//https://docs.mapbox.com/android/navigation/examples/show-current-location/
class MapsFragment : Fragment()
{
    private lateinit var binding: FragmentMapsBinding
    private lateinit var progressManager: ProgressManager
    private val PERMISSION_CODE = 200
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private var isTracking = false
    private var busMarker: PointAnnotation? = null

    private val viewModel: MapViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var mapbox: MapboxMap

    private val args: MapsFragmentArgs by navArgs()
    var route/*: Flight*/: Route? = null

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapsBinding.inflate(inflater)

        progressManager = ProgressManager(binding.parent, requireActivity())
        progressManager.start()

        route = Storage.routes[args.id]
        mapView = binding.map
        mapbox = mapView.getMapboxMap()

        mapbox.loadStyleUri(Style.MAPBOX_STREETS) {
            dataReady()
            addRouteOnMap()
        }
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun dataReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        if (route == null) return
        progressManager.finish()

        (requireActivity() as MainActivity).setActionBarTitle(route!!.name)
        checkLocationPermissions()

        if (route!!.positions[0] != null)
            tpCamera(geoPosToPoint(route!!.positions[0]))
        if (!route?.id.isNullOrEmpty())
            startListeningTracker(route!!.id) // включаю вебсокет


        binding.myLocationButton.setOnClickListener {
            checkLocationPermissions()
        }
        binding.findBusButton.setOnClickListener {
            if (busMarker == null)
                Snackbar.make(requireView(), getString(R.string.bus_not_connected), Snackbar.LENGTH_LONG).show()
            else
                moveCamera(busMarker!!.point)
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

    private fun addRouteOnMap()
    {
        Log.d("addRouteOnMap", "drawing route...")

        val busStopIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null)!!)

        val annotationApi = mapView.annotations
        val polylineManager = annotationApi.createPolylineAnnotationManager()
        val busStopManager = annotationApi.createPointAnnotationManager()

        val points = mutableListOf<Point>()
        route?.positions?.forEach { points.add(Point.fromLngLat(it.longitude, it.latitude)) } // добавляем точки для линии
        val polylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(points.toList())
            .withLineColor( ResourcesCompat.getColor(requireContext().resources, R.color.polyline, null) )
            .withLineWidth(5.0)

        val busStops = route!!.busStopsWithTime
        for (i in busStops.indices) // добавляем маркеры на карту
        {
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(
                    Point.fromLngLat(
                        busStops[i].busStop!!.position!!.longitude,
                        busStops[i].busStop!!.position!!.latitude
                    )
                )
                .withData(JsonParser.parseString(Json.encodeToString(i)))
                .withIconImage(busStopIcon!!)

            busStopManager.addClickListener(OnPointAnnotationClickListener { // listener нажатия на остановку на карте
                val idBusStop = it.getData()?.asInt
                if (idBusStop != null)
                {
                    val requestKey = requireContext().getString(R.string.itemSelected)
                    val bottomSheet = BusStopsBottomSheet(idBusStop, busStops)
                    bottomSheet.show(childFragmentManager, "BottomSheetDialog")

                    childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { key, bundle ->
                        // listener нажатия на остановку в Bottom sheet
                        if (key == requestKey)
                        {
                            val selected = bundle.getInt(requireContext().getString(R.string.busStop_id))
                            moveCamera(
                                geoPosToPoint(route!!.busStopsWithTime[selected].busStop?.position!!)
                            )
                        }
                    }
                }


                true
            })

            busStopManager.create(pointAnnotationOptions)
        }
        polylineManager.create(polylineAnnotationOptions)

    }

    private fun startListeningTracker(trackerId: String)
    {
        Log.d("Tracker", "startListening, id = $trackerId")
        if (isTracking) return
        isTracking = true

        val busIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.bus_marker, null)!!)!!
        val busMarkerManager = mapView.annotations.createPointAnnotationManager()

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startWebSocket(trackerId).collect { busPosition ->
                Log.d("Tracker", "new pos $busPosition")
                if (busMarker == null)
                {
                    busMarker = busMarkerManager.create(
                            PointAnnotationOptions()
                                .withIconImage(busIcon)
                                .withPoint(geoPosToPoint(busPosition))
                        )
                }
                else {
                    busMarker!!.point = geoPosToPoint(busPosition)
                    busMarkerManager.update(busMarker!!)
                }
            }
        }
    }

    private fun stopListeningTracker() {
        Log.d("Tracker", "stopListening")
        isTracking = false
        viewModel.stopWebSocket()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            checkLocationPermissions()
        else
            Snackbar.make(requireView(), getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkLocationPermissions() {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
            } else
            { // разрешения выданы
                var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null)
                    moveCamera(Point.fromLngLat(location!!.longitude, location!!.latitude))
            }
        else {
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun moveCamera(point: Point?) {
        if (point == null) return
        val cancelable: Cancelable = mapView.camera.easeTo(
            CameraOptions.Builder().center(point).zoom(14.0).build(),
            MapAnimationOptions.Builder().duration(3000).build()
        )
    }

    private fun tpCamera(point: Point?) {
        if (point == null) return
        val cameraPosition = CameraOptions.Builder()
            .zoom(12.0)
            .center(point)
            .build()
        mapView.getMapboxMap().setCamera(cameraPosition)
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

    private fun geoPosToPoint(geoPosition: GeoPosition): Point =
        Point.fromLngLat(geoPosition.longitude, geoPosition.latitude)

}
