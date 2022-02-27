package com.dru128.timetable.worker.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
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
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.locationcomponent.location
import dru128.timetable.R
import dru128.timetable.databinding.FragmentMapsBinding
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MapsFragment : Fragment()
{
    private lateinit var binding: FragmentMapsBinding
    private lateinit var progressManager: ProgressManager
    private val PERMISSION_CODE = 200
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private var isTracking = false
    private val busMarkerManager by lazy {
        mapView.annotations.createPointAnnotationManager()
    }

    private var busMarker: PointAnnotation? = null
    private var busStopMarkers: MutableList<PointAnnotation> = mutableListOf()

    private val viewModel: MapViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var mapbox: MapboxMap

    private val args: MapsFragmentArgs  by navArgs()
    var route/*: Flight*/: Route? = null
    var cameraChangeListener: OnCameraChangeListener? = null
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
            it.localizeLabels(getCurrentLocale(requireContext()))
            dataReady()
            addRouteOnMap()
        }

        return binding.root
    }

    private fun dataReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        if (route == null) return
        progressManager.finish()

        (requireActivity() as MainActivity).setActionBarTitle(route!!.name)

        if (route!!.positions[0] != null)
            tpCamera(geoPosToPoint(route!!.positions[0]))
        if (!route?.id.isNullOrEmpty())
            startListeningTracker(route!!.id) // включаю вебсокет
        if (isGPSEnabled() && isGPSPermissionGranted())
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }


        binding.myLocationButton.setOnClickListener {
            moveToUserLocation()
        }
        binding.findBusButton.setOnClickListener {
            if (busMarker == null)
                Snackbar.make(requireView(), getString(R.string.bus_not_connected), Snackbar.LENGTH_LONG).show()
            else
                moveCamera(busMarker!!.point)
        }

        val connectivityManager = getSystemService(requireContext(), ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager!!.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager!!.registerNetworkCallback(request, networkCallback)
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback()
    {
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
            busStopMarkers.add(
                busStopManager.create(
                    PointAnnotationOptions()
                    .withPoint(
                        Point.fromLngLat(
                            busStops[i].busStop!!.position!!.longitude,
                            busStops[i].busStop!!.position!!.latitude
                        )
                    )
                    .withData(JsonParser.parseString(Json.encodeToString(i)))
                    .withIconImage(busStopIcon!!)
                    .withTextField(busStops[i].busStop?.name.toString())
                    .withTextAnchor(TextAnchor.BOTTOM)
                )
            )
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
        }
        val isZoomChange = MutableStateFlow(false)
        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                if (it)
                {
                    for (i in 0 until busStopMarkers.size)
                        busStopMarkers[i].textField = busStops[i].busStop?.name.toString()
                }
                else
                {
                    for (i in 0 until busStopMarkers.size)
                        busStopMarkers[i].textField = ""
                }
                busStopManager.update(busStopMarkers)

            }
        }
        cameraChangeListener = OnCameraChangeListener { cameraChanged ->
            isZoomChange.value = mapbox.cameraState.zoom > 13.0
        }
        mapbox.addOnCameraChangeListener(cameraChangeListener!!)
        polylineManager.create(polylineAnnotationOptions)
    }

    private fun startListeningTracker(trackerId: String)
    {
        Log.d("Tracker", "startListening, id = $trackerId")
        if (isTracking) return
        isTracking = true

        val busIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.bus_marker, null)!!)!!

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startWebSocket(trackerId).collect { busPosition ->
                Log.d("Tracker", "new pos $busPosition")
                if (busMarker == null)
                {
                    Log.d("Tracker", "bus marker is null")
                    busMarker = busMarkerManager.create(
                            PointAnnotationOptions()
                                .withIconImage(busIcon)
                                .withPoint(geoPosToPoint(busPosition))
                        )
                }
                else
                {
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
            moveToUserLocation()
        else
            Snackbar.make(requireView(), getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun moveToUserLocation()
    {
        if (isGPSEnabled())
            if (isGPSPermissionGranted())
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
            else
            { // разрешения выданы

                mapView.location.apply {
                    if (!enabled)  updateSettings { enabled = true }
                    if (!pulsingEnabled) updateSettings { pulsingEnabled = true }
                }

                val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLocation != null)
                {
                    moveCamera(Point.fromLngLat(gpsLocation.longitude, gpsLocation.latitude))
                    Log.d("user_location", "GPS: ${gpsLocation.latitude}, ${gpsLocation.longitude}")
                }
                else
                {
                    val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (networkLocation != null)
                    {
                        moveCamera(Point.fromLngLat(networkLocation.longitude, networkLocation.latitude))
                        Log.d("user_location", "GPS: ${networkLocation.latitude}, ${networkLocation.longitude}")
                    }
                }
            }
        else
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
    }

    private fun moveCamera(point: Point?) {
        if (point == null) return
        mapView.camera.easeTo(
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

    override fun onDestroy() {
        mapbox.removeOnCameraChangeListener(cameraChangeListener!!)
        super.onDestroy()
    }

    private fun geoPosToPoint(geoPosition: GeoPosition): Point =
        Point.fromLngLat(geoPosition.longitude, geoPosition.latitude)

    private fun isGPSPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

    private fun isGPSEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }
}
