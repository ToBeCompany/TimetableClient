package com.example.timetable.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timetable.EndPoint
import com.example.timetable.data.GeoPoint
import com.example.timetable.data.Route
import com.example.timetable.data.database.RouteDao
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


class MapViewModel(application : Application): AndroidViewModel(application)
{
    private val HOST = EndPoint.host
    private val urlFlight = EndPoint.protocol + HOST + EndPoint.routeById
    var webSocketSession: DefaultClientWebSocketSession? = null

    val flightClient = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }

    private fun providerKtorCLient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }
    fun startWebSocket(routeId: String) = flow<GeoPoint> {
            providerKtorCLient().webSocket(
                method = HttpMethod.Get,
                host = HOST,
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
            flightClient.get(urlFlight + id)
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