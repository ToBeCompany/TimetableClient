package com.dru128.timetable.admin.map.create_route

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.data.metadata.BusStop
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.map.MapFragment
import com.dru128.timetable.tools.DrawableConvertor
import com.dru128.timetable.tools.IDManager
import com.dru128.timetable.tools.ProgressManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import dru128.timetable.R
import dru128.timetable.databinding.FragmentCreateRouteBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class CreateRouteFragment : MapFragment(), CreateBusStopFromDialog
{
    private val dotIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.route_dot, null))!! }
    private val dotSelectedIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.route_dot_selected, null))!! }
    private val deleteMarkerIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.delete_marker, null))!! }
    private val busStopMarkerIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null))!! }

    private val pointAnnotationClickListener: OnPointAnnotationClickListener by lazy { addRouteDotClickListener() }
    private val mapClickListener: OnMapClickListener by lazy { addMapClickListener() }
    private val cameraChangeListener: OnCameraChangeListener by lazy { addCameraChangeListener() }

    private var adapter: BusStopAdminRecyclerAdapter = BusStopAdminRecyclerAdapter(
        { id -> deleteBusStop(id) },
        { busStop -> busStopChanged(busStop) },
        arrayOf(),
    )
    private lateinit var progressManager: ProgressManager

    private lateinit var binding: FragmentCreateRouteBinding
    private val viewModel: CreateRouteViewModel by viewModels()

    private var busStopMarkers = mutableListOf<PointAnnotation>()
    private var routeDots = listOf<PointAnnotation>()
    private var deleteDotMarker: PointAnnotation? = null
    private var routePolyline: PolylineAnnotation? = null

    val isZoomChange = MutableStateFlow(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentCreateRouteBinding.inflate(inflater)
        progressManager = ProgressManager(binding.parent, requireActivity())
        progressManager.start()

        mapView = binding.map
        super.onCreateView(inflater, container, savedInstanceState)

        return binding.root
    }

    override fun mapReady()
    {
        showCrosshair()
        collectRoutePoints()
        collectBusStops()
        binding.addRouteLine.setOnClickListener {
            removeSelectingFromDots()
            viewModel.addRouteDot(mapbox.cameraState.center)
        }


        binding.addBusStop.setOnClickListener {
            val dialog = CreateBusStopDialog()
            dialog.show(childFragmentManager, CreateBusStopDialog.TAG)
        }

        binding.routeName.addTextChangedListener { viewModel.routeName = it.toString() }

        addCreateRouteButtonListener()



        val args: CreateRouteFragmentArgs by navArgs()
        if (args.jsonRoute.isNotEmpty())
        {
            Log.d("getRoute", "from navigation args")
            Json.decodeFromString<Route>(args.jsonRoute).let { _route ->
                viewModel.routeId = _route.id
                viewModel.routeName = _route.name

                viewModel.setRouteDots(geoPosToPoint(_route.positions))
                viewModel.setBusStops(_route.busStopsWithTime)
                binding.routeName.setText(_route.name)

                for (curBusStop in _route.busStopsWithTime)
                    busStopMarkers.add(
                        pointAnnotationManager.create(
                            createBusStop(
                                curBusStop.busStop, busStopMarkerIcon
                            )
                        )
                    )
            }
        }


        binding.routesRecyclerView.adapter = adapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                Log.d("zoom", it.toString())
                if (it)
                {
                    for (curBusStop in busStopMarkers)
                        curBusStop.textField =
                            viewModel.busStops.value
                                .find { it.busStop.id ==  curBusStop.getData()?.asString }
                                ?.busStop?.name.toString()


                    for (i in routeDots.indices)
                        routeDots[i].iconImageBitmap = dotIcon
                }
                else
                {
                    for (pointAnnotation in busStopMarkers)
                        pointAnnotation.textField = ""


                    for (routeDot in routeDots)
                        routeDot.iconImageBitmap = null

                    removeSelectingFromDots()
                }
                pointAnnotationManager.update(busStopMarkers)
                pointAnnotationManager.update(routeDots)
            }
        }

        progressManager.finish()
    }

    private fun collectRoutePoints()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.routePoints.collectLatest {
                drawRoutePolyline(it)
            }
        }
    }

    private fun collectBusStops()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.busStops.collectLatest {
                adapter.dataSet = it.toTypedArray()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun addCreateRouteButtonListener()
    {
        binding.createRouteButton.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                if (viewModel.routeId.isBlank())
                {
                    val status = viewModel.createRoute()
                    Log.d("status", "= $status")
                    if (status)
                        Navigation.findNavController(requireActivity(), R.id.nav_host_main).popBackStack()
                    else
                        Snackbar.make(requireView(), requireContext().resources.getString(R.string.error_create_route), Snackbar.LENGTH_LONG).show()
                }
                else
                {
                    val status = viewModel.editRoute()
                    Log.d("status", "= $status")
                    if (status)
                        Navigation.findNavController(requireActivity(), R.id.nav_host_main).popBackStack()
                    else
                        Snackbar.make(requireView(), requireContext().resources.getString(R.string.error_edit_route), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteBusStop(busStopId: String)
    {
        Log.d("action", "delete busStop | id = $busStopId")
        viewModel.deleteBusStop(busStopId)

        for (busStopMarker in busStopMarkers)
            busStopMarker.getData()?.let {
                if (it.asString == busStopId)
                {
                    pointAnnotationManager.delete(busStopMarker)
                    busStopMarkers.remove(busStopMarker)
                }
            }
    }

    private fun busStopChanged(newBusStop: BusStopWithTime)
    {
        Log.d("action", "busStopChanged")
        viewModel.changeBusStop(newBusStop)

        for (busStopMarker in busStopMarkers)
            busStopMarker.getData()?.let {
                if (it.asString == newBusStop.busStop.id)
                {
                    busStopMarker.textField = newBusStop.busStop.name
                    pointAnnotationManager.update(busStopMarker)
                }
            }
    }


    private fun addMapClickListener(): OnMapClickListener = OnMapClickListener { _routeDotAnnotation ->
        Log.d("click", "on map")
        removeSelectingFromDots()
        true
    }

    private fun addCameraChangeListener(): OnCameraChangeListener = OnCameraChangeListener { cameraChanged ->
        isZoomChange.value = mapbox.cameraState.zoom > 13.0
    }

    private fun addRouteDotClickListener(): OnPointAnnotationClickListener = OnPointAnnotationClickListener { pointAnnotation ->

        Log.d("click", "on point annotation")
        foreachDots@ for (routeDot in routeDots)
        {
            if (pointAnnotation == routeDot) // опредилили точка на которую нажали
            {
                removeSelectingFromDots()
                if (!routeDot.getData()!!.asBoolean) // нажали на невыбранную точку
                {
                    Log.d("click", "on route dot")
                    routeDot.iconImageBitmap = dotSelectedIcon
                    routeDot.setData(JsonParser.parseString(true.toString()))
                    pointAnnotationManager.update(routeDot)
                    showDeleteDotMarker(routeDot.point)
                }
                break@foreachDots
            }
        }
        if (pointAnnotation == deleteDotMarker)
        {
            Log.d("click", "on delete marker")
            deleteDotMarker?.point?.let { deleteDotRoute(it) }
        }
        true
    }

    private fun deleteDotRoute(point: Point)
    {
        removeSelectingFromDots()
        viewModel.deleteRouteDot(point)
    }

    private fun drawRoutePolyline(points: List<Point>)
    {
        if (routePolyline == null)
        {
            if (points.size > 1)
            {
                routePolyline = polylineAnnotationManager.create(createRouteLine(points))
            }
        }
        else
        {
            if (points.size > 1)
            {
                routePolyline!!.points = points
                polylineAnnotationManager.update(routePolyline!!)
            }
            else
            {
                polylineAnnotationManager.delete(routePolyline!!)
                routePolyline = null
            }
        }

        drawRouteDots(points)
    }

    private fun drawRouteDots(points: List<Point>)
    {
        hideRouteDots()

        routeDots = List<PointAnnotation>(points.size) { i ->
            pointAnnotationManager.create(
                PointAnnotationOptions()
                    .withIconImage(dotIcon)
                    .withPoint(points[i])
                    .withData( JsonParser.parseString(false.toString()) ) // isSelected
            )
        }
    }

    private fun hideRouteDots()
    {
        pointAnnotationManager.delete(routeDots)
    }

    private fun removeSelectingFromDots()
    {
        for (routeDot in routeDots)
            if (routeDot.getData() != null && routeDot.getData()!!.asBoolean)
            {
                routeDot.iconImageBitmap = dotIcon
                routeDot.setData(JsonParser.parseString(false.toString()))
                pointAnnotationManager.update(routeDot)
                if (deleteDotMarker != null)
                    pointAnnotationManager.delete(deleteDotMarker!!)
            }
    }

    private fun showDeleteDotMarker(position: Point)
    {
        Log.d("action", "showDeleteDotMarker")
        deleteDotMarker = pointAnnotationManager.create(
            PointAnnotationOptions()
                .withIconImage(deleteMarkerIcon)
                .withGeometry(position)
                .withIconAnchor(IconAnchor.BOTTOM)
        )
    }

    override fun createNewBusStop(name: String, time: String)
    {
        val busStop = BusStopWithTime(
            busStop = BusStop(
                id = IDManager.generateID(),
                name = name,
                position = pointToGeoPos(mapbox.cameraState.center)
            ),
            time = time
        )
        Log.d("createBusStop", busStop.toString())


        viewModel.addBusStop(busStop)

        busStopMarkers.add(
            pointAnnotationManager.create(
                createBusStop(
                    busStop.busStop, busStopMarkerIcon
                )
            )
        )
    }

    override fun onStart() {
        pointAnnotationManager.addClickListener(addRouteDotClickListener())
        mapbox.addOnMapClickListener(mapClickListener)
        mapbox.addOnCameraChangeListener(cameraChangeListener)
        super.onStart()
    }

    override fun onStop() {
        pointAnnotationManager.removeClickListener(pointAnnotationClickListener)
        mapbox.removeOnMapClickListener(mapClickListener)
        mapbox.removeOnCameraChangeListener(cameraChangeListener)
        super.onStop()
    }

    private fun findBusStopById(id: String): BusStopWithTime?
         = viewModel.busStops.value.find { it.busStop.id == id }


    private fun showCrosshair() {
        val crosshair = View(requireContext())
        crosshair.layoutParams = FrameLayout.LayoutParams(10, 10, Gravity.CENTER)
        crosshair.setBackgroundColor(Color.BLUE)
        mapView.addView(crosshair)
    }
}