package com.example.timetable.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.EndPoint
import com.example.timetable.data.GeoPosition
import com.example.timetable.data.Route
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.Security


class MapViewModel(application : Application): AndroidViewModel(application)
{
    private val HOST = EndPoint.host
    private val urlFlight = EndPoint.protocol + HOST + EndPoint.routeById

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
    fun startWebSocket(routeId: String) = flow<GeoPosition> {
            val webSocketClient = providerKtorCLient().webSocket(
                method = HttpMethod.Get,
                host = HOST,
                path = (EndPoint.webSocket_passenger + routeId)
            )
            {

                Log.d("start_websocket", "websocket start client map routeId = $routeId")
                for(frame in incoming)
                {
                    if (frame is Frame.Text)
                    {
                            val position = Json.decodeFromString<GeoPosition>(frame.readText())
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
}