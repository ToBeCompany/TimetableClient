package com.dru128.timetable

import dru128.timetable.BuildConfig

object EndPoint
{
    // протокол сервера
    const val protocol = "https://"

    // HOST сервера
    val host = if (BuildConfig.BUILD_TYPE == "release")
        "fierce-woodland-54822.herokuapp.com"
    else
        "safe-reaches-68287.herokuapp.com"

    // вебсокет пассажира
    const val webSocket_passenger = "passenger/"
    // вебсокет водителя
    const val webSocket_driver = "driver/"
    // вебсокет водителя
    const val webSocket_admin = "adminWebSocket/"

    // получить маршрут по ID (после передать ID маршрута)
    const val routeById = "/OneMarsh/"

    // получить коллекцию <имен и ID> маршрутов
    const val routes_names_id = "/namesMarsh"

    // получить все маршруты
    const val all_routes = "/allMarsh"

    // маршруты которые не отслежтваются
    const val not_tracking_routes = "/allMarsh"

    // получить данные пользователя по ID
    const val auth = "/sign/"

    // удалить маршрут по ID
    const val deleteRoute = "/deleteRoute/"

    // удалить маршрут по ID
    const val createRoute = "/createRoute/"

    // удалить маршрут по ID
    const val editRoute = "/editRoute/"

    // удалить пользователя по ID
    const val deleteUser = "/deleteUser/"

    // добавить (создать) пользователя
    const val createUser = "/addUser"

    // получить всех пользователей
    const val allUsers = "/allUsers"

    // получить автобусы
    const val getBuses = "/getBuses"
}