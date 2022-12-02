package com.dru128.timetable.worker.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dru128.timetable.MainActivity
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.map.BusStopsBottomSheet
import com.dru128.timetable.map.MapFragment
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.tools.ProgressManager
import com.google.android.material.snackbar.Snackbar
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.locationcomponent.location
import dru128.timetable.R
import dru128.timetable.databinding.FragmentMapWorkerBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class MapWorkerFragment: MapFragment()
{
    private lateinit var binding: FragmentMapWorkerBinding
    private lateinit var progressManager: ProgressManager

    private val busIcon : Bitmap by lazy {
        DrawableConvertor().drawableToBitmap(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bus_marker,
                null
            )!!
        )!!
    }

    private var busMarker: PointAnnotation? = null
    var busStopMarkers = listOf<PointAnnotation>()

    private val viewModel: MapWorkerViewModel by viewModels()
    private val args: MapWorkerFragmentArgs  by navArgs()
    val route/*: Flight*/:Route by lazy {
        RouteWorkerStorage.routes.find { it.id == args.id }!!
    }

    private var cameraChangeListener: OnCameraChangeListener? = null
    private val pointClickListener: OnPointAnnotationClickListener by lazy { addBusStopClickListener() }


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentMapWorkerBinding.inflate(inflater)

        progressManager = ProgressManager(binding.parent, requireActivity())
        progressManager.start()

        mapView = binding.workerMap
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun mapReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        addRouteOnMap()
        progressManager.finish()

        (requireActivity() as MainActivity).setActionBarTitle(route.name)

        tpCamera(route.positions.firstOrNull()) // наводим камеру на первую остановку маршрута
        startListeningTracker(route.id) // включаю вебсокет
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
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.geoPosition.collect { busPosition ->
                if (busPosition != null) {
                    Log.d("Tracker", "new pos $busPosition")
                    binding.findBusButton.setBackgroundResource(R.drawable.bus_in_gps_icon)
                    if (busMarker == null) {
                        Log.d("Tracker", "bus marker is null")
                        busMarker = pointAnnotationManager.create(
                            PointAnnotationOptions()
                                .withIconImage(busIcon)
                                .withPoint(geoPosToPoint(busPosition))
                        )
                    } else {
                        busMarker!!.point = geoPosToPoint(busPosition)
                        pointAnnotationManager.update(busMarker!!)
                    }
                }
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback()
    {
        // сеть доступна для использования
        override fun onAvailable(network: Network) {
            startListeningTracker(route.id)
            Log.d("network", "is on")
            super.onAvailable(network)
        }

        // соединение прервано
        override fun onLost(network: Network) {
//            stopListeningTracker()
            Log.d("network", "is off")
            super.onLost(network)
        }
    }

    private fun addRouteOnMap()
    {
        Log.d("addRouteOnMap", "drawing route...")
        val busStopIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null)!!)!!

        val positions = route.positions
        val busStops = route.busStopsWithTime

        if (positions.isEmpty())
            Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_route_line_null), Snackbar.LENGTH_LONG).show()
        else
            polylineAnnotationManager.create(createRouteLine(geoPosToPoint(positions))) // построение линии маршруты

        busStopMarkers = List<PointAnnotation>(route.busStopsWithTime.size) { i ->
            pointAnnotationManager.create(
                createBusStop(busStops[i].busStop, busStopIcon)
            )
        }

        val isZoomChange = MutableStateFlow(false)
        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                if (it)
                {
                    for (i in busStopMarkers.indices)
                        busStopMarkers[i].textField = busStops[i].busStop.name
                }
                else
                {
                    for (element in busStopMarkers)
                        element.textField = ""
                }
                pointAnnotationManager.update(busStopMarkers)
            }
        }

        cameraChangeListener = OnCameraChangeListener { cameraChanged ->
            isZoomChange.value = mapbox.cameraState.zoom > 13.0
        }

        pointAnnotationManager.addClickListener(pointClickListener)
        mapbox.addOnCameraChangeListener(cameraChangeListener!!)
    }

    private fun startListeningTracker(trackerId: String)
    {
        if (viewModel.isTracking) return
        Log.d("Tracker", "startListening, id = $trackerId")

        viewModel.startWebSocket(trackerId)
    }

    private fun addBusStopClickListener(): OnPointAnnotationClickListener = OnPointAnnotationClickListener { _busStopAnnotation ->
        // listener нажатия на остановку на карте
        for (busStop in busStopMarkers)
            if (_busStopAnnotation == busStop && _busStopAnnotation.getData() != null)
            {
                val requestKey = requireContext().getString(R.string.itemSelected)
                val busStopId = busStop.getData()!!.asString

                val bottomSheet = BusStopsBottomSheet(busStopId, route.busStopsWithTime)
                bottomSheet.show(childFragmentManager, "BottomSheetMap $busStopId")
                childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { key, bundle ->
                    // listener нажатия на остановку в Bottom sheet
                    if (key == requestKey)
                    {
                        val position = Json.decodeFromString<GeoPosition>(bundle.getString(requireContext().getString(R.string.busStop_id)).toString())
                        moveCamera(position)
                    }
                }

            }
        true
    }

    private fun stopListeningTracker() {
        Log.d("Tracker", "stopListening")
        viewModel.stopWebSocket()
    }

    override fun onDestroyView() {
        if (cameraChangeListener != null)
            mapbox.removeOnCameraChangeListener(cameraChangeListener!!)
        super.onDestroyView()
    }

    override fun onStart() {
        startListeningTracker(route.id)
        super.onStart()
    }

    override fun onStop() {
        stopListeningTracker()
        super.onStop()
    }

    override fun onDestroy() {
        pointAnnotationManager.removeClickListener(pointClickListener)
        super.onDestroy()
    }
}