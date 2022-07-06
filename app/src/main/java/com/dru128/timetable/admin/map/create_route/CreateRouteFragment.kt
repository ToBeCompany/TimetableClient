package com.dru128.timetable.admin.map.create_route

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.OrientationEventListener
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
import com.dru128.timetable.data.metadata.GeoPosition
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
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import dru128.timetable.R
import dru128.timetable.databinding.FragmentCreateRouteBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class CreateRouteFragment : MapFragment(), CreateBusStopFromDialog
{
    private val dotIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.route_dot, null))!! }
    private val dotSelectedIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.route_dot_selected, null))!! }
    private val deleteMarkerIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.delete_marker, null))!! }
    private val busStopMarkerIcon by lazy { DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, R.drawable.location_marker, null))!! }

    private val routeDotClickListener: OnPointAnnotationClickListener by lazy { addRouteDotClickListener() }
    private val mapClickListener: OnMapClickListener by lazy { addMapClickListener() }
    private val cameraChangeListener: OnCameraChangeListener by lazy { addCameraChangeListener() }

    private var adapter: BusStopAdminRecyclerAdapter = BusStopAdminRecyclerAdapter(
        { id -> deleteBusStop(id) },
        { busStop -> busStopChanged(busStop) },
        arrayOf(),
    )

    private lateinit var binding: FragmentCreateRouteBinding
    private val viewModel: CreateRouteViewModel by viewModels()

    private var busStopMarkers = mutableListOf<PointAnnotation>()
    private var routeDots = mutableListOf<PointAnnotation>()
    private var deleteDotMarker: PointAnnotation? = null
    private var routePolyline: PolylineAnnotation? = null

    val isZoomChange = MutableStateFlow(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentCreateRouteBinding.inflate(inflater)
        val progressManager = ProgressManager(binding.root, requireActivity())
        progressManager.start()

        mapView = binding.map
        super.onCreateView(inflater, container, savedInstanceState)
        showCrosshair()
        progressManager.finish()



        return binding.root
    }


    override fun mapReady()
    {
        binding.addRouteLine.setOnClickListener {
            val position = mapbox.cameraState.center
            createDotRoute(position)
        }

        binding.addBusStop.setOnClickListener {
            val dialog = CreateBusStopDialog()
            dialog.show(childFragmentManager, CreateBusStopDialog.TAG)
        }

        binding.routeName.addTextChangedListener { viewModel.route.name = it.toString() }

        binding.createRouteButton.setOnClickListener {
            val route = viewModel.route
            lifecycleScope.launch {
                if (route.id.isBlank())
                {
                    route.id = IDManager.generateID()
                    val status = viewModel.createRoute(route)
                    Log.d("status", "= $status")
                    if (status)
                    {
                        Navigation.findNavController(requireActivity(), R.id.nav_host_main).popBackStack()
                    }
                    else {
                        route.id = ""
                        Snackbar.make(requireView(), requireContext().resources.getString(R.string.error_create_route), Snackbar.LENGTH_LONG).show()
                    }
                }
                else
                {
                    val status = viewModel.editRoute(route)
                    Log.d("status", "= $status")
                    if (status)
                        Navigation.findNavController(requireActivity(), R.id.nav_host_main).popBackStack()
                    else
                        Snackbar.make(requireView(), requireContext().resources.getString(R.string.error_edit_route), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        Log.d("isInitRoute", viewModel.isInitRoute.toString())

        if (viewModel.isInitRoute)
            drawRoute(viewModel.route)
        else
        {
            val args: CreateRouteFragmentArgs by navArgs()
            if (args.jsonRoute.isNotEmpty())
            {
                Log.d("getRoute", "from navigation args")
                viewModel.route = Json.decodeFromString<Route>(args.jsonRoute)
                drawRoute(viewModel.route)
            }
            viewModel.isInitRoute = true
        }

        binding.routesRecyclerView.adapter = adapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launchWhenStarted {
            isZoomChange.collectLatest {
                if (it) {
                    for (i in busStopMarkers.indices)
                        busStopMarkers[i].textField =
                            viewModel.route.busStopsWithTime[i].busStop.name

                    for (i in routeDots.indices)
                        routeDots[i].iconImageBitmap = dotIcon
                }
                else {
                    for (pointAnnotation in busStopMarkers)
                        pointAnnotation.textField = ""


                    for (i in routeDots.indices)
                        routeDots[i].iconImageBitmap = null

                    removeSelectingFromDots()
                }
                pointAnnotationManager.update(busStopMarkers)
                pointAnnotationManager.update(routeDots)
            }
        }

        pointAnnotationManager.addClickListener(routeDotClickListener)
        mapbox.addOnMapClickListener(mapClickListener)
        mapbox.addOnCameraChangeListener(cameraChangeListener)
    }


    private fun deleteBusStop(id: String)
    {
        val position = viewModel.route.busStopsWithTime.indexOf( findBusStopById(id) )


        viewModel.route.busStopsWithTime
            .filter { it.busStop.id != id }
            .let {
                viewModel.route.busStopsWithTime = it
                adapter.dataSet = it.toTypedArray()
            }
        adapter.apply {
//            notifyDataSetChanged()
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,  viewModel.route.busStopsWithTime.size)
        }

        pointAnnotationManager.delete(busStopMarkers[position])
        busStopMarkers.removeAt(position)
        Log.d("deleteBusStop", "id = $id")
    }

    private fun busStopChanged(newBusStop: BusStopWithTime)
    {
        Log.d("event", "busStopChanged")
        for (i in 0 until viewModel.route.busStopsWithTime.size)
            if (viewModel.route.busStopsWithTime[i].busStop.id == newBusStop.busStop.id)
            {
                Log.d("event", "find busStop")

                viewModel.route.busStopsWithTime[i].time = newBusStop.time
                viewModel.route.busStopsWithTime[i].busStop.name = newBusStop.busStop.name

                busStopMarkers[i].textField = newBusStop.busStop.name
                pointAnnotationManager.update(busStopMarkers[i])

            }
        viewModel.route.busStopsWithTime.forEach {
            Log.d("bsSutp", it.busStop.name)

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

        forDots@ for (routeDot in routeDots)
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
                break@forDots
            }
            else if (pointAnnotation == deleteDotMarker)
            {
                Log.d("click", "on delete marker")
                deleteDotMarker?.point?.let { deleteDotRoute(it) }
                break@forDots

            }
        }
        true
    }

    private fun deleteDotRoute(point: Point)
    {
        removeSelectingFromDots()

        routeDots.find {
            it.point.latitude() == point.latitude() && it.point.longitude() == point.longitude()
        }?.let {
            pointAnnotationManager.delete(it)
            routeDots.removeAt(routeDots.indexOf(it))
        }
        viewModel.points.let { points ->
            points.remove(point)
            drawRoutePolyline(points)
            viewModel.route.positions = List(points.size) { i ->
                GeoPosition(
                    latitude = points[i].latitude(),
                    longitude = points[i].longitude()
                )
            }
        }

    }

    private fun createDotRoute(point: Point)
    {
        removeSelectingFromDots()
        viewModel.points.let { points ->
            points.add(point)
            drawRoutePolyline(points)
            viewModel.route.positions = List(points.size) { i ->
                GeoPosition(
                    latitude = points[i].latitude(),
                    longitude = points[i].longitude()
                )
            }
        }
        routeDots.add(
            pointAnnotationManager.create(
                PointAnnotationOptions()
                    .withIconImage(dotIcon)
                    .withPoint(point)
                    .withData( JsonParser.parseString(false.toString()) ) // isSelected
            )
        )
    }

    private fun drawRoutePolyline(points: List<Point>)
    {
        if (routePolyline == null)
        {
            if (points.size > 1)
            {
                val polylineAnnotationOptions = PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor( ResourcesCompat.getColor(requireContext().resources, R.color.polyline, null) )
                    .withLineWidth(5.0)
                routePolyline = polylineAnnotationManager.create(polylineAnnotationOptions)
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
        deleteDotMarker = pointAnnotationManager.create(
            PointAnnotationOptions()
                .withIconImage(deleteMarkerIcon)
                .withGeometry(position)
                .withIconAnchor(IconAnchor.BOTTOM)
        )
        Log.d("event", "showDeleteDotMarker")
    }

    private fun drawRoute(route: Route)
    {
        Log.d("event", "drawRoute")
        binding.routeName.setText(route.name)

        viewModel.points = mutableListOf()
        for (position in route.positions)
        {
            val point = geoPosToPoint(position)
            viewModel.points.add(point)
            routeDots.add(
                pointAnnotationManager.create(
                    PointAnnotationOptions()
                        .withIconImage(dotIcon)
                        .withPoint(point)
                        .withData( JsonParser.parseString(false.toString()) ) // isSelected
                )
            )
        }

        drawRoutePolyline(viewModel.points)

        for (busStop in route.busStopsWithTime)
            busStopMarkers.add(
                pointAnnotationManager.create(
                    createBusStop(
                        busStop.busStop, busStopMarkerIcon
                    )
                )
            )

        adapter.dataSet = route.busStopsWithTime.toTypedArray()
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


        viewModel.route.busStopsWithTime += busStop
        viewModel.route.busStopsWithTime.let { busStops ->
            adapter.dataSet = busStops.toTypedArray()
//            adapter.notifyDataSetChanged()
            adapter.notifyItemInserted(busStops.lastIndex)
            adapter.notifyItemRangeChanged(busStops.lastIndex, busStops.size)
        }

        busStopMarkers.add(
            pointAnnotationManager.create(
                createBusStop(
                    busStop.busStop, busStopMarkerIcon
                )
            )
        )
    }

    override fun onStop() {
        pointAnnotationManager.removeClickListener(routeDotClickListener)
        mapbox.removeOnMapClickListener(mapClickListener)
        mapbox.removeOnCameraChangeListener(cameraChangeListener)
        super.onStop()
    }

    private fun findBusStopById(id: String): BusStopWithTime?
            = viewModel.route.busStopsWithTime.find { it.busStop.id == id }


    private fun showCrosshair() {
        val crosshair = View(requireContext())
        crosshair.layoutParams = FrameLayout.LayoutParams(10, 10, Gravity.CENTER)
        crosshair.setBackgroundColor(Color.BLUE)
        mapView.addView(crosshair)
    }

}