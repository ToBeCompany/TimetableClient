package com.example.timetable.worker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.EndPoint
import com.example.timetable.data.RouteDataManager
import com.example.timetable.data.metadata.Route
import com.example.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*


class RouteViewModel(application : Application): AndroidViewModel(application)
{
    private val urlFlightNames = EndPoint.protocol + EndPoint.host + EndPoint.routes_names_id
    private val urlAllRoutes = EndPoint.protocol + EndPoint.host + EndPoint.all_routes
    private var dataManager = RouteDataManager()

    val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }


    suspend fun getRoutes(): List<Route>?
    {
        val fromCache = getFromCache()

        if (fromCache.isNullOrEmpty())
        {
            val fromServer = getFromServer()

            if (fromServer.isNullOrEmpty()) return null

            dataManager.updateRoutes(fromServer)
            return fromServer
        }
        else
            return fromCache
    }

    suspend fun getFlight(): List<FlightsNameResponse>? =
        try {
            client.get(urlFlightNames)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }


    private suspend fun getFromServer(): List<Route>? =
        try {
            client.get(urlAllRoutes)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }

    private suspend fun getFromCache(): List<Route> =
        dataManager.getRoutes()
}