package com.dru128.timetable.admin.map.dispacher

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.admin.AdminMainFragmentDirections
import com.dru128.timetable.admin.map.RouteAdminStorage
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
import dru128.timetable.R
import dru128.timetable.databinding.FragmentDispatcherBinding
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.indexOf
import kotlin.collections.indices
import kotlin.collections.isEmpty
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.toList
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
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
        val progressManager = ProgressManager(binding.root, requireActivity())
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
            if (RouteAdminStorage.routes.isEmpty())
            {
                Log.d("request", "get routes from server")
                val status: Boolean = viewModel.getRoutes()
                Log.d("status", "= $status")
                if (status)
                {
                    startListeningTracker()
                    Log.d("data", "routes size = ${RouteAdminStorage.routes.size}")
                    val dispatcherRoutes = Array<DispatcherRouteItem> ( RouteAdminStorage.routes.size) {
                        DispatcherRouteItem(RouteAdminStorage.routes[it], false)
                    }
                    adapter.dataSet = dispatcherRoutes                }
                else
                    Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_get_routes), Snackbar.LENGTH_LONG).show()
            }
            else
            {
                Log.d("request", "get routes from repository")
                startListeningTracker()
                val dispatcherRoutes = Array<DispatcherRouteItem> ( RouteAdminStorage.routes.size) {
                    DispatcherRouteItem(RouteAdminStorage.routes[it], false)
                }
                adapter.dataSet = dispatcherRoutes
            }
            binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.routesRecyclerView.adapter = adapter
        }
    }

    override fun mapReady()
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

        lifecycleScope.launch {
            viewModel.isVisibleRoutePanel.collect {
                binding.routePanel.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        binding.createRouteButton.setOnClickListener { v ->
            Navigation.findNavController(requireActivity(), R.id.nav_host_main)
                .navigate(AdminMainFragmentDirections.actionAdminMainFragmentToCreateRouteFragment(""))
        }

        initRecyclerView()
    }

    private fun drawBuses()
    {
        val busIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, dru128.timetable.R.drawable.bus_marker, null)!!)!!

        lifecycleScope.launch {

//            RouteAdminStorage.buses.collect { busesPos ->
//                Log.d("UPDATE_BUS_POS", busesPos.values.toString())
//
//                for ((name, position) in busesPos) {
//
//                    if (RouteAdminStorage.busMarkers.containsKey(name)) {
//                        RouteAdminStorage.busMarkers[name]?.point = geoPosToPoint(position)
//                        pointAnnotationManager.update(RouteAdminStorage.busMarkers[name]!!)
//
//                    } else {
//                        val busMarker = pointAnnotationManager.create(
//                            PointAnnotationOptions()
//                                .withIconImage(busIcon)
//                                .withPoint(geoPosToPoint(position))
//                        )
//                        pointAnnotationManager.update(busMarker)
//                        RouteAdminStorage.busMarkers[name] = busMarker
//                    }
//                }
//            }
        }
    }

    private fun showRoute(route: Route)
    {
        Log.d("showRoute", route.name)
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

        val isZoomChange = MutableStateFlow(false)
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

        cameraChangeListener = OnCameraChangeListener { cameraChanged ->
            isZoomChange.value = mapbox.cameraState.zoom > 13.0
        }
        mapbox.addOnCameraChangeListener(cameraChangeListener!!)

        RouteAdminStorage.mapboxRoutes[route.id] = MapboxRoute(
            isVisible = true,
            trackLine = routeLine,
            busStops = busStopMarkers
        )
    }

    private fun addBusStopClickListener(): OnPointAnnotationClickListener = OnPointAnnotationClickListener { _busStopAnnotation ->
        // listener нажатия на остановку на карте
        for (mapboxRoute in RouteAdminStorage.mapboxRoutes)
            for (busStop in mapboxRoute.value.busStops)
                if (_busStopAnnotation == busStop && _busStopAnnotation.getData() != null)
                {
                    val requestKey = requireContext().getString(R.string.itemSelected)
                    val routeId = mapboxRoute.key
                    val busStopId = busStop.getData()?.asString.toString()

                    val route = findRouteById(routeId)
                    if (route != null)
                    {
                        val bottomSheet = BusStopsBottomSheet(busStopId, route.busStopsWithTime)
                        bottomSheet.show(childFragmentManager, "BottomSheetMap $busStopId $routeId")
                        childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { key, bundle ->
                            // listener нажатия на остановку в Bottom sheet
                            if (key == requestKey)
                            {
                                val position = Json.decodeFromString<GeoPosition>(bundle.getString(requireContext().getString(R.string.busStop_id)).toString())
                                moveCamera(position)
                            }
                        }
                    }
                }
        true
    }

    private fun findRouteById(id: String): Route?
        = RouteAdminStorage.routes.find { _route -> _route.id == id }


    private fun hideRoute(id: String)
    {
        RouteAdminStorage.mapboxRoutes[id]?.let { mapboxRoute ->
            mapboxRoute.isVisible = false
            pointAnnotationManager.delete(mapboxRoute.busStops.toList())
            polylineAnnotationManager.delete(mapboxRoute.trackLine)
            RouteAdminStorage.mapboxRoutes.remove(id)
        }

//        RouteAdminStorage.mapboxRoutes.remove(id)
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
                        val position = RouteAdminStorage.routes.indexOf( findRouteById(routeId) )

                        adapter.apply {
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position,  RouteAdminStorage.routes.size)
                        }
                        hideRoute(routeId)
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
        Navigation.findNavController(requireActivity(), R.id.nav_host_main)
            .navigate(
                AdminMainFragmentDirections.actionAdminMainFragmentToCreateRouteFragment(
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

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startWebSocket()
        }

        for ((id, busLocation) in viewModel.buses)
        {
            lifecycleScope.launch {
                busLocation.isActual.collect { isActual ->
                    Log.d("isActualChanged", isActual.toString())
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
        for (mapboxRoute in RouteAdminStorage.mapboxRoutes)
            if (mapboxRoute.value.isVisible)
                findRouteById(mapboxRoute.key)?.let { _route ->
                    showRoute(_route)
                }
        pointAnnotationManager.addClickListener(pointClickListener)

        if (!viewModel.isTracking)
            startListeningTracker()

        super.onStart()
    }

    override fun onStop() {
        pointAnnotationManager.removeClickListener(pointClickListener)
        if (viewModel.isTracking)
            stopListeningTracker()
        super.onStop()
    }
}