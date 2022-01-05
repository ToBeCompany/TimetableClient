package com.example.timetable.map


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
    private var id = "1"
    private val HOST = "fierce-woodland-54822.herokuapp.com"
    private val websocketPath = "/passenger/$id"
    private val flightPath = "/flight/"

    private var urlFlight = "https://$HOST/OneMarsh/"


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
    fun startWebSocket() = flow<GeoPosition> {
            val webSocketClient = providerKtorCLient().webSocket(
                method = HttpMethod.Get,
                host = HOST,
                path = websocketPath
            )
            {

                Log.d("STARTWEBSOCKET", "websocket start client map")
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