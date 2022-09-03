package com.dru128.timetable.admin.map.dispacher

import com.dru128.timetable.data.metadata.Route

data class DispatcherRouteItem(
    var route: Route,
    var isOnline: Boolean = false,
    var isVisible: Boolean = false
)