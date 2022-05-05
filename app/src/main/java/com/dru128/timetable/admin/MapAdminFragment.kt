package com.dru128.timetable.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dru128.timetable.MapFragment
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.DrawableConvertor
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import dru128.timetable.databinding.FragmentMapAdminBinding
import kotlinx.coroutines.launch


class MapAdminFragment : MapFragment()
{
    private lateinit var binding: FragmentMapAdminBinding
    private val viewModel: MapAdminViewModel by viewModels()

    private val busMarkerManager by lazy {
        mapView.annotations.createPointAnnotationManager()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentMapAdminBinding.inflate(inflater)

        mapView = binding.adminMap
        super.onCreateView(inflater, container, savedInstanceState)

        lifecycleScope.launch {
            viewModel.getBuses()
            drawBuses()

        }
        return binding.root
    }

    override fun dataReady()
    {


    }

    fun drawBuses()
    {
        Log.d("fun start", "drawBuses")

        val buses = viewModel.buses
        val busIcon = DrawableConvertor().drawableToBitmap(ResourcesCompat.getDrawable(resources, dru128.timetable.R.drawable.bus_marker, null)!!)!!

        for ((name, position) in buses)
        {
            Log.d("Server", position.toString())

//            if (viewModel.busMarkers.containsKey(name))
//            {
//                busMarkerManager.update(viewModel.busMarkers[name]!!)
//
//            }
//            else {
                var busMarker = busMarkerManager.create(
                    PointAnnotationOptions()
                        .withIconImage(busIcon)
                        .withPoint(geoPosToPoint(position))
                )
            busMarkerManager.update(busMarker)
            viewModel.busMarkers.put(
                    name,
                    busMarker
                )
//            }
        }
    }

    fun drawRoute(route: Route){}
}