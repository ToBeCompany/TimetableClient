package com.example.timetable.data

import kotlinx.serialization.Serializable


@Serializable
class Flight( // рейс
    var name: String,
    var id: String,
    var bus: Bus,
    var route: Route,
    var driver: User
)