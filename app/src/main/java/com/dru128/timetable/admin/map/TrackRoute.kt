package com.dru128.timetable.admin.map

import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import kotlinx.coroutines.flow.MutableStateFlow

class TrackRoute(
    var route: Route,
    var position: MutableStateFlow<GeoPosition?> = MutableStateFlow(null)
)