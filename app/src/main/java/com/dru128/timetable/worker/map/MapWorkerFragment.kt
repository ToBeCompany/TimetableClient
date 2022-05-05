package com.dru128.timetable.worker.map

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dru128.timetable.MainActivity
import com.dru128.timetable.MapFragment
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.worker.BusStopsBottomSheet
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
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
import dru128.timetable.databinding.FragmentMapWorkerBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MapWorkerFragment: MapFragment()
{
    private lateinit var binding: FragmentMapWorkerBinding
    private lateinit var progressManager: ProgressManager

    private var isTracking = false
    private val busMarkerManager by lazy {
        mapView.annotations.createPointAnnotationManager()
    }

    private var busMarker: PointAnnotation? = null
    private var busStopMarkers: MutableList<PointAnnotation> = mutableListOf()

    private val viewModel: MapWorkerViewModel by viewModels()

    private val args: MapWorkerFragmentArgs  by navArgs()
    var route/*: Flight*/: Route? = null
    var cameraChangeListener: OnCameraChangeListener? = null

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapWorkerBinding.inflate(inflater)

        progressManager = ProgressManager(binding.parent, requireActivity())
        progressManager.start()

        route = Storage.routes[args.id]
        mapView = binding.workerMap
        super.onCreateView(inflater, container, savedInstanceState)


        return binding.root
    }

    override fun dataReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        addRouteOnMap()
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

}
