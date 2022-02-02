package com.dru128.timetable.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.GeoPoint
import com.dru128.timetable.data.Route
import com.dru128.timetable.data.database.RouteDao
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.Security


class MapViewModel(application : Application, private val routeDao: RouteDao): AndroidViewModel(application)
{
    var webSocketSession: DefaultClientWebSocketSession? = null

    fun startWebSocket(routeId: String) = flow<GeoPoint> {
            Storage.websocketClient().webSocket(
                method = HttpMethod.Get,
                host = EndPoint.host,
                path = (EndPoint.webSocket_passenger + routeId)
            )
            {
                webSocketSession = this@webSocket
                Log.d("start_websocket", "start client map routeId = $routeId")
                for(frame in incoming)
                {
                    if (frame is Frame.Text)
                    {
                            val position = Json.decodeFromString<GeoPoint>(frame.readText())
                            emit(position)
                            Log.d("websocket", "update position: " + position.latitude.toString())
                    }
                }
            }

    }.flowOn(Dispatchers.IO)

    suspend fun getFlight(id: String): /*Flight*/Route? =
        try {
            Storage.client.get(EndPoint.routeById + id)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }

    fun stopWebSocket()
    {
        viewModelScope.launch {
            Log.d("closeWebSocket", "user close map fragment")
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "user close map fragment"))
        }
    }

    fun addRoute(route: Route)
    {
        viewModelScope.launch {
//            routeDao.insert(route)
        }
    }

//    private fun getNewRouteEntry(itemName: String, itemPrice: String, itemCount: String): Item {
//        return Route(
//            itemName = itemName,
//            itemPrice = itemPrice.toDouble(),
//            quantityInStock = itemCount.toInt()
//        )
//    }
}

class MapViewModelFactory(private val itemDao: RouteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        if (modelClass.isAssignableFrom(MapViewModel::class.java))
        {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(Application(), itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}