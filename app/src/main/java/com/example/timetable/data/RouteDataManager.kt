package com.example.timetable.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


class RouteDataManager
{
    private val pathDatabase = "app/src/main/assets/route_database"
    private var database = File(pathDatabase)
// программно создавать файл
    suspend fun updateRoutes(routes: List<Route>)
    {

        database.writeText( Json.encodeToString(routes) )
    }

    suspend fun getRoutes(): List<Route> =
        Json.decodeFromString<List<Route>>( database.readText() )

    fun clearData()
    {
        database.writeText("")
    }
}