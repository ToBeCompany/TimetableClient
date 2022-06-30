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
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import dru128.timetable.R
import dru128.timetable.databinding.FragmentDispatcherBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString


class DispatcherFragment : MapFragment()
{
    private lateinit var binding: FragmentDispatcherBinding
    private val viewModel: DispatcherViewModel by viewModels()

    private var adapter: RouteAdminRecyclerAdapter = RouteAdminRecyclerAdapter(
        { route -> showRoute(route) },
        { id -> hideRoute(id) },
        { id -> deleteRoute(id) },
        { id -> editRoute(id) },
        arrayOf(),
    )

    private val busMarkerManager by lazy {
        mapView.annotations.createPointAnnotationManager()
    }
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
                    Snackbar.make(requireView(), requireContext().resources.getString(R.string.error_get_routes), Snackbar.LENGTH_LONG).show()
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
//                if (binding.showPanel.rotation == 180f && it)
            }
        }

        binding.createRouteButton.setOnClickListener { v ->
            Navigation.findNavController(requireActivity(), R.id.nav_host_main)
                .navigate(AdminMainFragmentDirections.actionAdminMainFragmentToCreateRouteFragment(""))
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
        val busStopIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null)!!)!!

        val routeLine = polylineAnnotationManager.create(
            createRouteLine(route.positions)
        )

        val busStopMarkers = List<PointAnnotation>(route.busStopsWithTime.size) { i ->
            pointAnnotationManager.create(
                createBusStop(route.busStopsWithTime[i].busStop, busStopIcon)
            )
        }

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
            RouteAdminStorage.mapboxRoutes.remove(id)
        }

//        RouteAdminStorage.mapboxRoutes.remove(id)
    }

    fun deleteRoute(routeId: String)
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
                        val position = RouteAdminStorage.routes.value.indexOf( findRouteById(routeId) )

                        adapter.apply {
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position,  RouteAdminStorage.routes.value.size)
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

    fun editRoute(id: String)
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

    override fun onStart() {
        for (mapboxRoute in RouteAdminStorage.mapboxRoutes)
            if (mapboxRoute.value.isVisible)
                findRouteById(mapboxRoute.key)?.let { _route ->
                    showRoute(_route)
                }
        pointAnnotationManager.addClickListener(pointClickListener)
        super.onStart()
    }

    override fun onStop() {
        pointAnnotationManager.removeClickListener(pointClickListener)
        super.onStop()
    }
}