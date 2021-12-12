package com.example.timetable.map


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.App
import com.example.timetable.data.BusData
import com.example.timetable.data.n.User
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class MapViewModel(application : Application): AndroidViewModel(application)
{
    private val PORT = 8080
    private val HOST = "fierce-woodland-54822.herokuapp.com/passanger/1"
    private val PATH = "/passiger"

    val client = HttpClient(CIO) {
        install(WebSockets)
    }


    var busData: BusData? = null
    val busLocation: Flow<LatLng> = flow {
        for (i in 0..200) {
            emit(LatLng(
                i.toDouble()/5,
                50.0
            ))
            kotlinx.coroutines.delay(300)
        }
    }

//    fun updateLocation(newData: LatLng) { busLocation.value = Resource.Success(newData) }
        //https://habr.com/ru/post/432310/

    fun startWebSocket() = flow<String> {
            client.webSocket(urlString = HOST){
                while (true) {
                    when(val frame = incoming.receive()){
                        is Frame.Text -> {
                            Log.d("getDAta", frame.readText())
//                            val data : User= Json.decodeFromString<User>(frame.readText())
//                            emit(frame.readText().toString())
                                //https://question-it.com/questions/2640377/kak-serializovat-web-socket-frametext-v-ktor-s-pomoschju-kotlinxserialization
                        }
                        else -> TODO()
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

    }

}