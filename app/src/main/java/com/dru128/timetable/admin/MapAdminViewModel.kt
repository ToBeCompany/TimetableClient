package com.dru128.timetable.admin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import io.ktor.client.request.get

class MapAdminViewModel(application : Application): AndroidViewModel(application)
{
    var routes = arrayOf<Route>()
    var buses = mapOf<String, GeoPosition>()
    var busMarkers = mutableMapOf<String, PointAnnotation>()

    suspend fun getRoutes(): Array<Route>? =
        try {
            Log.d("Server", "SUCCESS")
            routes = Storage.client.get(EndPoint.all_routes)
            routes
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }

    suspend fun getBuses()
    {
        buses = mapOf<String, GeoPosition>(
            "bus 1" to GeoPosition(53.3, 86.6),
            "bus 2" to GeoPosition(53.34, 86.67),
            "bus 3" to GeoPosition(53.348, 86.673)
        )
    }


//    suspend fun getBuses(): Map<String, GeoPosition>? =
//        try {
//            Log.d("Server", "SUCCESS")
//            buses = Storage.client.get(EndPoint.getBuses)
//            buses
//        }
//        catch (error: Exception) {
//            Log.d("Server", "ERROR: ${error.message}")
//            null
//        }
}