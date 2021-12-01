package com.example.timetable.data.n

class Flight( // маршрут
    var name: String,
    var id: String,
    var bus: Bus,
    var route: Route,
    var driver: User
)