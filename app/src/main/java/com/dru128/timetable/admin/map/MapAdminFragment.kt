package com.dru128.timetable.admin.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.MapFragment
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.BusStopsBottomSheet
import com.dru128.timetable.RouteAndBusStopId
import com.dru128.timetable.data.metadata.GeoPosition
import com.google.android.material.snackbar.Snackbar
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import dru128.timetable.R
import dru128.timetable.databinding.FragmentMapAdminBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


/**
 * После переключения на нижней навигации перестают работать скрытие маршрутов
 */

//mapView.getMapboxMap().cameraState.center
class MapAdminFragment : MapFragment()
{
    private lateinit var binding: FragmentMapAdminBinding
    private val viewModel: MapAdminViewModel by viewModels()

    private lateinit var progressManager: ProgressManager

    private var adapter: RouteAdminRecyclerAdapter = RouteAdminRecyclerAdapter(
        { route -> showRoute(route) },
        { id -> hideRoute(id) },
        arrayOf(),
    )

    private val busMarkerManager by lazy {
        mapView.annotations.createPointAnnotationManager()
    }
    var cameraChangeListener: OnCameraChangeListener? = null
    val pointClickListener: OnPointAnnotationClickListener by lazy { addBusStopClickListener() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentMapAdminBinding.inflate(inflater)
        progressManager = ProgressManager(binding.root, requireActivity())
        progressManager.start()

        mapView = binding.adminMap
        super.onCreateView(inflater, container, savedInstanceState)

//        lifecycleScope.launch { // тут включается вебсокет
//            viewModel.startWebSocket().collect {
//                Log.d("web socket admin", it.toString())
//            }
//        }

        progressManager.finish()

        return binding.root
    }

    private fun initRecyclerView()
    {

        lifecycleScope.launch {
            if (RouteAdminStorage.routes.value.isEmpty())
            {
                Log.d("request", "get routes from server")
                    val status: Boolean = viewModel.getRoutes()
                    Log.d("status", "= $status")
                    if (status)
                    {
                        Log.d("data", "routes size = ${RouteAdminStorage.routes.value.size}")
                        adapter.dataSet = RouteAdminStorage.routes.value
                    }
                    else
                        Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_get_users), Snackbar.LENGTH_LONG).show()
            }
            else
            {
                Log.d("request", "get routes from repository")
                adapter.dataSet = RouteAdminStorage.routes.value
            }
            binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.routesRecyclerView.adapter = adapter
        }
    }

    override fun mapReady()
    {
        binding.showPanel.setOnClickListener { v ->
            if (binding.routePanel.visibility == View.VISIBLE)
            {
                binding.routePanel.visibility = View.GONE
            }
            else
            {
                binding.routePanel.visibility = View.VISIBLE
            }
            lifecycleScope.launch {
                for (i in 1..90)
                {
                    v.rotation += 2f
                    delay(1L)
                }
            }
        }

        binding.createRouteButton.setOnClickListener {

        }

        initRecyclerView()
    }

    private fun drawBuses() {
        val busIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, dru128.timetable.R.drawable.bus_marker, null)!!)!!

        lifecycleScope.launch {
            RouteAdminStorage.buses.collect { busesPos ->
                Log.d("UPDATE_BUS_POS", busesPos.values.toString())

                for ((name, position) in busesPos) {

                    if (RouteAdminStorage.busMarkers.containsKey(name)) {
                        RouteAdminStorage.busMarkers[name]?.point = geoPosToPoint(position)
                        busMarkerManager.update(RouteAdminStorage.busMarkers[name]!!)

                    } else {
                        val busMarker = busMarkerManager.create(
                            PointAnnotationOptions()
                                .withIconImage(busIcon)
                                .withPoint(geoPosToPoint(position))
                        )
                        busMarkerManager.update(busMarker)
                        RouteAdminStorage.busMarkers[name] = busMarker
                    }
                }
            }
        }
    }

    fun showRoute(route: Route)
    {
        Log.d("showRoute", route.name)
        var busStopTitles = arrayOf<View>()
        var busStopMarkers: Array<PointAnnotation> = arrayOf()

        val busStopIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null)!!)!!


        val positions = route.positions
        val busStops = route.busStopsWithTime

        for (i in busStops.indices) // добавляем маркеры на карту
        {
            val busStop = pointAnnotationManager.create(
                createBusStop(busStops[i].busStop, route.id,busStopIcon)
            )
            busStopMarkers += busStop
            val busStopTitle = createBusStopTitle(busStops[i].busStop, busStop)
            busStopTitles += busStopTitle


            pointAnnotationManager.clickListeners
        }
        val isZoomChange = MutableStateFlow(false)
        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                if (it)
                {
                    for (title in busStopTitles)
                        viewAnnotationManager.updateViewAnnotation(
                            title,
                            viewAnnotationOptions { visible(true) }
                        )
                }
                else
                {
                    for (title in busStopTitles)
                        viewAnnotationManager.updateViewAnnotation(
                            title,
                            viewAnnotationOptions { visible(false) }
                        )
                }
            }
        }

        cameraChangeListener = OnCameraChangeListener { cameraChanged ->
            isZoomChange.value = mapbox.cameraState.zoom > 13.0
        }
        mapbox.addOnCameraChangeListener(cameraChangeListener!!)


        RouteAdminStorage.mapboxRoutes[route.id] = MapboxRoute(
            isVisible = true,
            trackLine = polylineAnnotationManager.create(createRouteLine(positions)),
            busStops = busStopMarkers,
            busStopTitles = busStopTitles
        )
    }

    private fun addBusStopClickListener(): OnPointAnnotationClickListener = OnPointAnnotationClickListener {
        // listener нажатия на остановку на карте
        if (it.getData() != null)
        {
            val requestKey = requireContext().getString(R.string.itemSelected)
            val ids = Json.decodeFromString<RouteAndBusStopId>(
                it.getData().toString()
            )
            val route = findRouteById(ids.routeId)
            if (route != null)
            {
                val bottomSheet = BusStopsBottomSheet(ids.busStopId, route.busStopsWithTime)
                bottomSheet.show(childFragmentManager, "BottomSheetMap ${ids.busStopId} ${ids.routeId}")
                childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { key, bundle ->
                    // listener нажатия на остановку в Bottom sheet
                    if (key == requestKey)
                    {
                        val position = Json.decodeFromString<GeoPosition>(bundle.getString(requireContext().getString(R.string.busStop_id)).toString())
                        moveCamera( geoPosToPoint(position) )
                    }
                }
            }
        }
        true
    }

    private fun findRouteById(id: String): Route?
        = RouteAdminStorage.routes.value.find { _route -> _route.id == id }


    fun hideRoute(id: String)
    {
        RouteAdminStorage.mapboxRoutes[id]?.let { mapboxRoute ->
            mapboxRoute.isVisible = false
            pointAnnotationManager.delete(mapboxRoute.busStops.toList())
            polylineAnnotationManager.delete(mapboxRoute.trackLine)
            for (title in mapboxRoute.busStopTitles)
                viewAnnotationManager.removeViewAnnotation(title)
            RouteAdminStorage.mapboxRoutes.remove(id)
        }
//        RouteAdminStorage.mapboxRoutes.remove(id)
    }

    override fun onStart() {
//        for (mapboxRoute in RouteAdminStorage.mapboxRoutes)
//            if (mapboxRoute.value.isVisible)
//                findRouteById(mapboxRoute.key)?.let { _route ->
//                    showRoute(_route)
//                }
        pointAnnotationManager.addClickListener(pointClickListener)
        super.onStart()
    }

    override fun onStop() {
        pointAnnotationManager.removeClickListener(pointClickListener)
        super.onStop()
    }
}