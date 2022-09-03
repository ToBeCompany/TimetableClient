package com.dru128.timetable.admin.map

import com.dru128.timetable.admin.map.dispacher.MapboxRoute
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.Resource
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import kotlinx.coroutines.flow.MutableStateFlow


object RouteAdminStorage {
    var routes: MutableStateFlow< Resource< Array<Route> > > =
        MutableStateFlow(Resource.Success(arrayOf<Route>()))

    var mapboxRoutes = mutableMapOf<String, MapboxRoute>()

    var busMarkers = mutableMapOf<String, PointAnnotation>()
}