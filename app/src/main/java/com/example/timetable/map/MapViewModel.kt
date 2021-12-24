package com.example.timetable.map


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.data.Flight
import com.example.timetable.data.GeoPosition
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
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

    val flightClient = HttpClient(CIO) {
        install(JsonFeature)
    }

//    fun updateLocation(newData: LatLng) { busLocation.value = Resource.Success(newData) }
        //https://habr.com/ru/post/432310/
    private fun providerKtorCLient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }
    fun startWebSocket() = flow<GeoPosition> {
            val client = providerKtorCLient().webSocket(
                method = HttpMethod.Get,
                host = HOST,
                path = websocketPath
            )
            {

                Log.d("START", "STARTWEBSOCKET")
                for(frame in incoming){
                    when (frame){
                        is Frame.Text -> {
//                            val data : User= Json.decodeFromString<User>(frame.readText())
                            var data = Json.decodeFromString<GeoPosition>(frame.readText())
                            emit(data)
                            Log.d("newData", data.latitude.toString())
                                //https://question-it.com/questions/2640377/kak-serializovat-web-socket-frametext-v-ktor-s-pomoschju-kotlinxserialization
                        }
                    }
                }
//                    for (response in incoming)
//                    {
//                        when(response){
//                            is Frame.Binary -> {
////                                (response.data.decode() as LatLng)
//                            }
//                        }
//                        var data = response.readBytes()
//                        //emit(response.receive readBytes() as LatLng) // !_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_
//
//                    }
            }

    }.flowOn(Dispatchers.IO)

    suspend fun getFlight(id: String): Flight = flightClient.get(HOST + flightPath)
}