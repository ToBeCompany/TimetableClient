package com.dru128.timetable.admin.map.dispacher

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.map.BusStopsBottomSheet
import com.dru128.timetable.map.MapFragment
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.tools.Resource
import com.google.android.material.snackbar.Snackbar
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import dru128.timetable.R
import dru128.timetable.databinding.FragmentDispatcherBinding
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class DispatcherFragment : MapFragment()
{
    private val busIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.bus_marker, null))!! }
    private val progressManager: ProgressManager by lazy { ProgressManager(binding.root, requireActivity()) }
    private val isZoomChange = MutableStateFlow(false)

    private lateinit var binding: FragmentDispatcherBinding
    private val viewModel: DispatcherViewModel by viewModels()
    private var adapter: RouteAdminRecyclerAdapter = RouteAdminRecyclerAdapter(
        { route -> showRoute(route) },
        { id -> hideRoute(id) },
        { id -> deleteRoute(id) },
        { id -> editRoute(id) },
        arrayOf(),
    )

    private var cameraChangeListener: OnCameraChangeListener? = null
    private val pointClickListener: OnPointAnnotationClickListener by lazy { addBusStopClickListener() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentDispatcherBinding.inflate(inflater)
        progressManager.start()

        mapView = binding.adminMap
        super.onCreateView(inflater, container, savedInstanceState)

        return binding.root
    }

    override fun mapReady()
    {
        addShowPanelButtonListener()
        addCreateRouteButtonListener()
        addRefreshListener()
        
        collectRoutes()
        loadRoutes()

        binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.routesRecyclerView.adapter = adapter

        progressManager.finish()
    }

    private fun loadRoutes()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.getRoutes()
        }
    }

    private fun addRefreshListener()
    {
        binding.swipeRefresh.setOnRefreshListener {
            loadRoutes()
        }
    }

    private fun addShowPanelButtonListener()
    {
        binding.showPanel.rotation = if (viewModel.isVisibleRoutePanel.value) 0f else 180f
        binding.showPanel.setOnClickListener { v ->
            viewModel.isVisibleRoutePanel.value = !viewModel.isVisibleRoutePanel.value
            lifecycleScope.launch {
                for (i in 1..90)
                {
                    v.rotation += 2f
                    delay(1L)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.isVisibleRoutePanel.collect {
                binding.routePanel.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
    }

    private fun addCreateRouteButtonListener()
    {
        binding.createRouteButton.setOnClickListener { v ->
            v.findNavController()
                .navigate(DispatcherFragmentDirections.actionMapAdminFragmentToCreateRouteFragment(""))
        }
    }

    private fun collectRoutes()
    {
        lifecycleScope.launchWhenStarted {
            RouteAdminStorage.routes.collectLatest { response ->
                when (response)
                {
                    is Resource.Error -> {
                        binding.swipeRefresh.isRefreshing = false
                        Log.d("ErrorRoute", response.message.toString())
                        Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_get_routes), Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Loading -> {
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is Resource.Success -> { // данные маршрутов обновлены
                        Log.d("action", "get routes | size = ${response.data?.size}")
                        binding.swipeRefresh.isRefreshing = false
                        if (response.data != null)
                        {
                            stopListeningTracker()
                            hideAllRoutes()

                            initMapboxRoutes(response.data)


                            updateRouteList(response.data)

                            showAllActiveRoute(response.data)
                            startListeningTracker()
                        }
                    }
                }
            }
        }
    }

    private fun initMapboxRoutes(routes: Array<Route>)
    {
        for (route in routes)
            if (RouteAdminStorage.mapboxRoutes[route.id] == null)
                RouteAdminStorage.mapboxRoutes[route.id] = MapboxRoute()
    }


    private fun showRoute(route: Route)
    {
        Log.d("action", "show route: name = ${route.name}, id = ${route.id}")

        if (RouteAdminStorage.mapboxRoutes[route.id] != null &&
            isAnnotationInitInMapboxRoute(RouteAdminStorage.mapboxRoutes[route.id]!!))
            hideRoute(route.id)


        val busStopIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null)!!)!!

        val routeLine = polylineAnnotationManager.create(
            createRouteLine(route.positions)
        )

        val busStopMarkers = List<PointAnnotation>(route.busStopsWithTime.size) { i ->
            pointAnnotationManager.create(
                createBusStop(route.busStopsWithTime[i].busStop, busStopIcon)
            )
        }
        busStopMarkers.forEach { pointAnnotationManager.selectAnnotation(it) }

        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                if (it)
                    for (i in busStopMarkers.indices)
                        busStopMarkers[i].textField = route.busStopsWithTime[i].busStop.name
                else
                    for (element in busStopMarkers)
                        element.textField = ""
                pointAnnotationManager.update(busStopMarkers)
            }
        }


        RouteAdminStorage.mapboxRoutes[route.id] = MapboxRoute(
            isVisible = true,
            trackLine = routeLine,
            busStops = busStopMarkers
        )
    }

    private fun addBusStopClickListener(): OnPointAnnotationClickListener = OnPointAnnotationClickListener { _busStopAnnotation ->
        // listener нажатия на остановку на карте
        for (mapboxRoute in RouteAdminStorage.mapboxRoutes)
            mapboxRoute.value.busStops?.forEach { busStop ->
                if (_busStopAnnotation == busStop && _busStopAnnotation.getData() != null)
                {
                    val requestKey = requireContext().getString(R.string.itemSelected)
                    val routeId = mapboxRoute.key
                    val busStopId = busStop.getData()?.asString.toString()

                    val route = findRouteById(routeId)
                    if (route != null) {
                        val bottomSheet = BusStopsBottomSheet(busStopId, route.busStopsWithTime)
                        bottomSheet.show(childFragmentManager, "BottomSheetMap $busStopId $routeId")
                        childFragmentManager.setFragmentResultListener(
                            requestKey,
                            viewLifecycleOwner
                        ) { key, bundle ->
                            // listener нажатия на остановку в Bottom sheet
                            if (key == requestKey) {
                                val position = Json.decodeFromString<GeoPosition>(
                                    bundle.getString(requireContext().getString(R.string.busStop_id))
                                        .toString()
                                )
                                moveCamera(position)
                            }
                        }
                    }
                }
            }
        true
    }

    private fun findRouteById(id: String): Route?
        = RouteAdminStorage.routes.value.data?.find { _route -> _route.id == id }


    private fun hideRoute(id: String)
    {
        Log.d("action", "hide route: $id")
        RouteAdminStorage.mapboxRoutes[id]?.let { mapboxRoute ->
            if (isAnnotationInitInMapboxRoute(mapboxRoute))
            {
                mapboxRoute.isVisible = false
                pointAnnotationManager.delete(mapboxRoute.busStops!!.toList())
                polylineAnnotationManager.delete(mapboxRoute.trackLine!!)
//            RouteAdminStorage.mapboxRoutes.remove(id, mapboxRoute)
            }

        }
    }

    private fun deleteRoute(routeId: String)
    {
        Log.d("event", "show dialog delete route")
        AlertDialog.Builder(requireActivity())
            .setTitle(requireContext().resources.getString(R.string.delete_route))
//               .setIcon(R.drawable.hungrycat)
            .setNegativeButton(requireContext().resources.getString(R.string.cancel)) { dialog, _id -> dialog.dismiss() }
            .setPositiveButton(requireContext().resources.getString(R.string.confirm)) { dialog, _id ->
                Log.d("request", "delete user by id: $routeId")
                lifecycleScope.launch {
                    val status = viewModel.deleteRoute(routeId)
                    Log.d("status", "= $status")
                    if (status)
                    {
                        hideRoute(routeId)
                        removeBusLocationChangeListener(routeId)


                        RouteAdminStorage.routes.value.data?.let { _routes ->
                            val newRoutes = _routes.filter { it.id != routeId }
                            RouteAdminStorage.routes.value = Resource.Success(newRoutes.toTypedArray())

                            newRoutes.forEach { Log.d("routes:", it.name) }
                            adapter.dataSet = Array<DispatcherRouteItem> ( newRoutes.size) {
                                DispatcherRouteItem(newRoutes[it], false)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                    else
                        Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_delete_route), Snackbar.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun editRoute(id: String)
    {
        findNavController()
            .navigate(
                DispatcherFragmentDirections.actionMapAdminFragmentToCreateRouteFragment(
                    Json.encodeToString(
                        findRouteById(
                            id
                        )
                    )
                )
            )
    }



    private fun startListeningTracker()
    {
        if (viewModel.isTracking) return
        Log.d("Tracker", "start listening all buses")

        initBuses()

        lifecycleScope.launchWhenStarted {
            viewModel.startWebSocket()
        }

        for ((id, busLocation) in viewModel.buses)
        {
            lifecycleScope.launchWhenStarted {
                busLocation.isActual.collect { isActual ->
                    Log.d("isActualChanged", busLocation.isActual.toString())
                    if (isActual)
                    {
                        Log.d("addBusLocationChangeListener", "id = $id")
                        adapter.routePositionById(id)?.let {
                            adapter.dataSet[it].isOnline = true
                            adapter.notifyItemChanged(it)
                        }
                        busLocation.busLocationJob = addBusLocationChangeListener(busLocation, id)
                    }
                    else
                    {
                        Log.d("removeBusLocationChangeListener", "id = $id")
                        adapter.routePositionById(id)?.let {
                            adapter.dataSet[it].isOnline = false
                            adapter.notifyItemChanged(it)
                        }
                        removeBusLocationChangeListener(id)
                    }
                }
            }
        }
    }
    private fun initBuses()
    {
        val _buses: MutableMap<String, BusLocation> = mutableMapOf()
        RouteAdminStorage.routes.value.data?.forEach { _buses[it.id] = BusLocation() }
        viewModel.buses = _buses.toMap()
    }

    private fun stopListeningTracker()
    {
        Log.d("Tracker", "stop listening")
        viewModel.stopWebSocket()
        for ((id, busLocation) in viewModel.buses)
            removeBusLocationChangeListener(id)
    }


    private fun addBusLocationChangeListener(bus: BusLocation, id: String) = lifecycleScope.launch (/*start = CoroutineStart.LAZY*/)
    {
        bus.position.collectLatest { position ->
            Log.d("newLocation", bus.position.value.toString())
            if (position != null)
                if (RouteAdminStorage.busMarkers.containsKey(id))
                {
                    Log.d("BusMarker","update, id = $id")
                    RouteAdminStorage.busMarkers[id]?.point = geoPosToPoint(position)
                    pointAnnotationManager.update(RouteAdminStorage.busMarkers[id]!!)
                } else
                {
                    Log.d("BusMarker","create new, id = $id")
                    val busMarker = pointAnnotationManager.create(
                        PointAnnotationOptions()
                            .withIconImage(busIcon)
                            .withPoint(geoPosToPoint(position))
                            .withIconAnchor(IconAnchor.BOTTOM)
                            .withTextAnchor(TextAnchor.TOP)
                            .withTextSize(11.0)
                    )
                    pointAnnotationManager.update(busMarker)
                    RouteAdminStorage.busMarkers[id] = busMarker
                }
        }
    }

    private fun removeBusLocationChangeListener(id: String)
    {
        Log.d("BusMarker","remove, id = $id")
        viewModel.buses[id]?.busLocationJob?.cancel()
        RouteAdminStorage.busMarkers[id]?.let { busMarker ->
            pointAnnotationManager.delete(busMarker)
            RouteAdminStorage.busMarkers.remove(id, busMarker)
        }
    }


    override fun onStart()
    {
//        showAllActiveRoute()

        pointAnnotationManager.addClickListener(pointClickListener)
        cameraChangeListener = OnCameraChangeListener { cameraChanged ->
            isZoomChange.value = mapbox.cameraState.zoom > 13.0
        }
        mapbox.addOnCameraChangeListener(cameraChangeListener!!)

        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                if (it)
                    for ((key, value) in RouteAdminStorage.busMarkers) {
                        value.textField = findRouteById(key)?.name
                        pointAnnotationManager.update(value)
                    }
                else
                    for ((key, value) in RouteAdminStorage.busMarkers) {
                        value.textField = ""
                        pointAnnotationManager.update(value)
                    }
            }
        }
        super.onStart()
    }

    private fun showAllActiveRoute(routes: Array<Route>)
    {
        Log.d("action", "showAllActiveRoute")
        for (route in routes)
            if (RouteAdminStorage.mapboxRoutes[route.id]!!.isVisible)
                showRoute(route)
    }

    private fun hideAllRoutes()
    {
        for ((routeId, mapboxRoute) in RouteAdminStorage.mapboxRoutes)
            if (mapboxRoute.isVisible && isAnnotationInitInMapboxRoute(mapboxRoute))
            {
                pointAnnotationManager.delete(mapboxRoute.busStops!!)
                polylineAnnotationManager.delete(mapboxRoute.trackLine!!)
            }
    }

    private fun updateRouteList(routes: Array<Route>)
    {
        adapter.dataSet = Array<DispatcherRouteItem> (routes.size) {
            DispatcherRouteItem(
                routes[it],
                false,
                RouteAdminStorage.mapboxRoutes[routes[it].id]!!.isVisible
            )
        }
        adapter.notifyDataSetChanged()
    }


    override fun onStop() {
        pointAnnotationManager.removeClickListener(pointClickListener)

        super.onStop()
    }

    private fun isAnnotationInitInMapboxRoute(mapboxRoute: MapboxRoute): Boolean =
        mapboxRoute.trackLine != null && mapboxRoute.busStops != null
}