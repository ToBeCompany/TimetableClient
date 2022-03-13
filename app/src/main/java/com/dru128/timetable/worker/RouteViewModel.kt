package com.dru128.timetable.worker

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dru128.timetable.App
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.JsonDataManager
import com.dru128.timetable.data.metadata.Route
import io.ktor.client.request.get


class RouteViewModel: ViewModel()
{
    private var dataManager = JsonDataManager()

    suspend fun getRoutes(): Array<Route>?
    {
        val fromCache = dataManager.loadRoutes()

        if (fromCache.isNullOrEmpty())
        {
            val fromServer = getFromServer()
            if (fromServer.isNullOrEmpty()) return null
            dataManager.saveRoutes(fromServer)
            Log.d("Data", "get from server")

            return fromServer
        }
        else {
            Log.d("Data", "get from cash")

            return fromCache
        }
    }

/*    suspend fun getFlight(): List<FlightsNameResponse>? =
        try {
            Storage.client.get(EndPoint.routes_names_id)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }*/

    private suspend fun getFromServer(): Array<Route>? =
        try {
            Log.d("Server", "SUCCESS")
            Storage.client.get(EndPoint.all_routes)
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }
}