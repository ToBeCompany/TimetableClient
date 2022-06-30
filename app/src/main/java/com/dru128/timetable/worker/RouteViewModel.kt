package com.dru128.timetable.worker

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dru128.timetable.App
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.data.JsonDataManager
import com.dru128.timetable.data.metadata.Route
import io.ktor.client.request.get


class RouteViewModel: ViewModel()
{
    private var dataManager = JsonDataManager(App.globalContext)

    suspend fun getRoutes(): Array<Route>?
    {
//        val fromCache = dataManager.loadRoutes()
//
//        if (fromCache.isNullOrEmpty())
//        {
            val fromServer = getFromServer()
            if (fromServer.isNullOrEmpty()) return null
//            dataManager.saveRoutes(fromServer)
            Log.d("Data", "get from server")

            return fromServer
//        }
//        else {
//            Log.d("Data", "get from cash")
//
//            return fromCache
//        }
    }

    private suspend fun getFromServer(): Array<Route>? =
        try {
            Log.d("Server", "SUCCESS")
            Repository.client.get(EndPoint.all_routes)
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }
}