package com.example.timetable.data


class Flight( // маршрут
    var name: String,
    var id: String,
    var bus: Bus,
    var route: Route,
    var driver: User
)