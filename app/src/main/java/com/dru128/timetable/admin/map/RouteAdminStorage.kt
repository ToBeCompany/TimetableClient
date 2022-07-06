package com.dru128.timetable.admin.map

import com.dru128.timetable.admin.map.dispacher.MapboxRoute
import com.dru128.timetable.data.metadata.Route
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation


object RouteAdminStorage {
    var routes = arrayOf<Route>()
    var mapboxRoutes = mutableMapOf<String, MapboxRoute>()

    var busMarkers = mutableMapOf<String, PointAnnotation>()
}