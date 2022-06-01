package com.dru128.timetable.data.metadata

import kotlinx.serialization.Serializable


@Serializable
class Flight( // рейс
    var name: kotlin.String,
    var id: kotlin.String,
    var bus: Bus,
    var route: Route,
    var driver: User
)