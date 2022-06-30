package com.dru128.timetable.admin.map.dispacher

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.admin.edituser.UsersStorage
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DispatcherViewModel(application : Application): AndroidViewModel(application)
{
    var webSocketSession: DefaultClientWebSocketSession? = null
    var isVisibleRoutePanel = MutableStateFlow<Boolean>(true)

    suspend fun getRoutes(): Boolean
    {
        try {
            Log.d("Server", "SUCCESS")
            val _routes: Array<Route>? = Repository.client.get(EndPoint.all_routes)
            if (_routes.isNullOrEmpty()) return false
            RouteAdminStorage.routes.value = _routes
//            RouteAdminStorage.routes.value = arrayOf()
//            _routes.forEach { RouteAdminStorage.routes.value += TrackRoute(it) }
            return true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }



    suspend fun getBuses()
    {
/*        while (true)
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
            )
            delay(1000)
        }*/
    }


    fun startWebSocket() = flow<GeoPosition>
    {
        Repository.websocketClient().webSocket(
            method = HttpMethod.Get,
            host = "192.168.111.116",
            path = "all"
        )
        {
            webSocketSession = this@webSocket
            Log.d("WEB_SOCKET", "web socket admin start")
            for (frame in incoming)
            {
                if (frame is Frame.Text)
                {
                    Log.d("WEB_SOCKET", "new data:" + frame.readText())

//                    val position = Json.decodeFromString<GeoPosition>(frame.readText())
//                    emit(position)
//                    Log.d("WEB_SOCKET", "update position: " + position.latitude.toString())
                }
            }
        }

    }.flowOn(Dispatchers.IO)

    fun stopWebSocket()
    {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("WEB_SOCKET", "web socket admin close")
            webSocketSession?.close(
                CloseReason(
                    CloseReason.Codes.NORMAL,
                    "user close map fragment"
                )
            )
        }
    }

    suspend fun deleteRoute(id: String): Boolean =
        try {
            Log.d("Server", "SUCCESS")
            val response: Boolean? = Repository.client.get(EndPoint.deleteRoute + id)

            RouteAdminStorage.mapboxRoutes.remove(id)
            RouteAdminStorage.busMarkers.remove(id)
            RouteAdminStorage.routes.value
                .filter { it.id != id }
                .let { RouteAdminStorage.routes.value = it.toTypedArray() }
            true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            false
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