package com.dru128.timetable.admin.map

import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import kotlinx.coroutines.flow.MutableStateFlow

object RouteAdminStorage
{
    var routes = MutableStateFlow(arrayOf<Route>())
    var mapboxRoutes = mutableMapOf<String, MapboxRoute>()

    var buses = MutableStateFlow(mapOf<String, GeoPosition>())
    var busMarkers = mutableMapOf<String, PointAnnotation>()

}