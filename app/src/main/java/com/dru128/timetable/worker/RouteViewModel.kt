package com.dru128.timetable.worker

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dru128.timetable.App
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.data.JsonDataManager
import com.dru128.timetable.data.metadata.Route
import io.ktor.client.call.body
import io.ktor.client.request.get


class RouteViewModel: ViewModel()
{
    private var dataManager = JsonDataManager(App.globalContext)

    suspend fun getRoutes(): Array<Route>?
    {
        val serverVersion = getDBVersionFromServer()
        val cacheVersion = dataManager.getDBVersion()

        if (serverVersion != null)
            if (cacheVersion != null && cacheVersion == serverVersion)
            {
                Log.d("DATA", "get from cache")
                return dataManager.loadRoutes() // маршруты с сервера не изменились
            }
            else
            {
                Log.d("DATA", "get from server")
                getRoutesFromServer()?.let { fromServer ->
                    dataManager.saveRoutes(fromServer, serverVersion)
                    return  fromServer // маршруты получены с сервера и сохранены в кэш
                }
                return null // маршруты с сервера не удалось получить
            }
        else
            return null
    }

    fun isFavouritesContainsRealRoutes(routes: Array<Route>, favourites: Array<String>): Boolean
    {
        for (route in routes)
            if (favourites.contains(route.id))
                return true
        return false
    }

    private suspend fun getRoutesFromServer(): Array<Route>? =
        try {
            Repository.client.get(EndPoint.all_routes).body()
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }

    private suspend fun getDBVersionFromServer(): Int? =
        try {
            Repository.client.get(EndPoint.get_db_version).body()
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }

    fun addToFavourite(id: String)
    {
        Log.d("event", "add to favourite route, id = $id")
        dataManager.addToFavouriteRoutes(id)
    }

    fun deleteFromFavourite(id: String)
    {
        Log.d("event", "delete from favourite route, id = $id")
        dataManager.deleteFromFavouriteRoutes(id)
    }

    fun getFavouriteRoutes(): Array<String>
        = dataManager.loadFavouriteRoutes()
}