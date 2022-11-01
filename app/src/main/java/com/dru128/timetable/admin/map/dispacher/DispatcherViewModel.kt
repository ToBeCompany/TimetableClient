package com.dru128.timetable.admin.map.dispacher

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dru128.timetable.App
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.JsonDataManager
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.Resource
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class DispatcherViewModel : ViewModel()
{
    var buses: Map<String, BusLocation> = mapOf()

    var isVisibleRoutePanel = MutableStateFlow<Boolean>(true)

    var webSocketSession: DefaultClientWebSocketSession? = null
    var isTracking: Boolean = false

    private var dataManager = JsonDataManager(App.globalContext)

    suspend fun getRoutes()
    {
        RouteAdminStorage.routes.value = Resource.Loading()
        val serverVersion = getDBVersionFromServer()
        val cacheVersion = dataManager.getDBVersion()


        RouteAdminStorage.routes.value = if (serverVersion != null)
            if (cacheVersion != null && cacheVersion == serverVersion)
            {
                val fromCache = dataManager.loadRoutes()
                Log.d("ROUTE_DATA", "get from cache")
                if (fromCache.isNullOrEmpty())
                    Resource.Error("Не удалось получить маршруты с кэша") // маршруты с сервера не удалось получить
                else
                    Resource.Success(fromCache) // маршруты с сервера не изменились
            }
            else
            {
                val fromServer = getRoutesFromServer()
                Log.d("ROUTE_DATA", "get from server")
                if (fromServer.isNullOrEmpty())
                    Resource.Error("Не удалось получить маршруты с сервера") // маршруты с сервера не удалось получить
                else
                {
                    dataManager.saveRoutes(fromServer, serverVersion) // маршруты получены с сервера и сохранены в кэш
                    Resource.Success(fromServer)
                }
            }
        else
            Resource.Error("Не удалось получить версию БД")
    }

    private suspend fun getDBVersionFromServer(): Int? =
        try {
            Repository.client.get(EndPoint.get_db_version).body()
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }


    private suspend fun getRoutesFromServer(): Array<Route>?
    {
        try {
            val response: HttpResponse = Repository.client.get(EndPoint.all_routes)
            Log.d("Server", "Status code: ${response.status.value}")
            return if (response.status.value == 200)
            {
                val routes = response.body<Array<Route>>()
                Log.d("Server", "SUCCESS")
                routes
            }
            else
                null
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return null
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
            webSocketSession = this@webSocket
            Log.d("WEB_SOCKET", "web socket admin start")

            for (frame in incoming)
            {
                if (frame is Frame.Text)
                {
                    val busLocation = Json.decodeFromString<BusLocationResponse>(frame.readText())
                    Log.d("WEB_SOCKET", "update position: $busLocation")
                    if (busLocation.position != null)
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
                this.setBody(
                    mapOf("id" to id)
                )
            }
            Log.d("Server", "Status code: ${response.status.value}")

            return if (response.status.value == 200)
            {
                Log.d("Server", "SUCCESS")
                true
            } else
                false
        } catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }
}