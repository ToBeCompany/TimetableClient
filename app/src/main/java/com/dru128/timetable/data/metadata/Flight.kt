package com.dru128.timetable.data.metadata

import com.dru128.timetable.data.Route
import kotlinx.serialization.Serializable


@Serializable
class Flight( // рейс
    var name: String,
    var id: String,
    var bus: Bus,
    var route: Route,
    var driver: User
)