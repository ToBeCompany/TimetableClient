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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class MapAdminViewModel(application : Application): AndroidViewModel(application)
{
    var routes = arrayOf<Route>()
    var buses = MutableStateFlow(mapOf<String, GeoPosition>())
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
        while (true)
        {
            buses.value = mapOf<String, GeoPosition>(
                "bus 1" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 2" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 3" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 4" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 5" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 6" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 7" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 8" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 9" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 10" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 11" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble()),
                "bus 12" to GeoPosition((0..70).random().toDouble(), (0..70).random().toDouble())
            )
            delay(1000)
        }
    }


/**    suspend fun getBuses(): Map<String, GeoPosition>? =
        try {
            Log.d("Server", "SUCCESS")
            buses = Storage.client.get(EndPoint.getBuses)
            buses
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }*/
}