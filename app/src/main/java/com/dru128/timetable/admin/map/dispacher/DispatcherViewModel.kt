package com.dru128.timetable.admin.map.dispacher

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.metadata.Route
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class DispatcherViewModel : ViewModel()
{
    var buses: MutableMap<String, BusLocation> = mutableMapOf()

    var webSocketSession: DefaultClientWebSocketSession? = null
    var isVisibleRoutePanel = MutableStateFlow<Boolean>(true)
    var isTracking: Boolean = false

    suspend fun getRoutes(): Boolean
    {
        try {
            val response: HttpResponse = Repository.client.get(EndPoint.all_routes)
            Log.d("Server", "Status code: ${response.status.value}")
            if (response.status.value == 200)
            {
                val routes = response.body<Array<Route>>()
                RouteAdminStorage.routes = routes

                routes.forEach {
                    buses[it.id] = BusLocation()
                }
                Log.d("Server", "SUCCESS")
                return true
            }
            else
                return false
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }


    suspend fun startWebSocket()
    {
        isTracking = true
        Repository.websocketClient().webSocket(
            method = HttpMethod.Get,
            host = EndPoint.host,
            path = EndPoint.webSocket_dispatcher
        )
        {
            Log.d("WEB_SOCKET", "web socket admin start")
            webSocketSession = this@webSocket

            for (frame in incoming)
            {
                if (frame is Frame.Text)
                {
                    val busLocation = Json.decodeFromString<BusLocationResponse>(frame.readText())
                    Log.d("WEB_SOCKET", "update position: $busLocation")
                    buses[busLocation.id]?.position?.emit(busLocation.position)
                }
            }
        }

    }

    fun stopWebSocket()
    {
        isTracking = false
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

    suspend fun deleteRoute(id: String): Boolean
    {
        try {
            val response: HttpResponse = Repository.client.delete(EndPoint.deleteRoute) {
                this.setBody(id)
            }
            Log.d("Server", "Status code: ${response.status.value}")

            if (response.status.value == 200)
            {
                RouteAdminStorage.mapboxRoutes.remove(id)
                RouteAdminStorage.busMarkers.remove(id)
                RouteAdminStorage.routes
                    .filter { it.id != id }
                    .let { RouteAdminStorage.routes = it.toTypedArray() }
                Log.d("Server", "SUCCESS")
                return true
            } else
                return false
        } catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
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