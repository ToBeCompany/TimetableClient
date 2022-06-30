package com.dru128.timetable.map

import kotlinx.serialization.Serializable

@Serializable
class RouteAndBusStopId(
    val routeId: String,
    val busStopId: String
)