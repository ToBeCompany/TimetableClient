package com.dru128.timetable.worker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.Route
import com.dru128.timetable.data.RouteDataManager
import com.dru128.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get


class RouteViewModel(application : Application): AndroidViewModel(application)
{
    private var dataManager = RouteDataManager()

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
            Storage.client.get(EndPoint.routes_names_id)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }


    private suspend fun getFromServer(): List<Route>? =
        try {
            Storage.client.get(EndPoint.all_routes)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }

    private suspend fun getFromCache(): List<Route> =
        dataManager.getRoutes()
}