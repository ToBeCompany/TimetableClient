package com.example.timetable

object EndPoint
{
    // протокол сервера
    const val protocol = "https://"

    // HOST сервера
    const val host = "fierce-woodland-54822.herokuapp.com"

    // вебсокет пассажира
    const val webSocket_passenger = "passenger/"
    // вебсокет водителя
    const val webSocket_driver = "driver/"

    // получить маршрут по ID (после передать ID маршрута)
    const val routeById = "/OneMarsh/"

    // получить коллекцию <имен и ID> маршрутов
    const val routes_names_id = "/namesMarsh"

    // получить все маршруты
    const val all_routes = "/allMarsh"

    // маршруты которые не отслежтваются
    const val not_tracking_routes = "/allMarsh"
}