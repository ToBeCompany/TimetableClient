package com.example.timetable.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.Resource
import com.example.timetable.data.BusData
import com.example.timetable.data.n.Bus
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ObjectInput
import java.io.ObjectInputStream

class MapViewModel(application : Application): AndroidViewModel(application)
{
    private val PORT = 8080
    private val HOST = "127.0.0.1"
    private val PATH = "/chat"

    val client = HttpClient {
        install(WebSockets)
    }


    val busLocation: Flow<LatLng> = flow {
        for (i in 0..200) {
            emit(LatLng(
                i.toDouble()/5,
                50.0
            ))
            delay(300)
        }
    }
    var busData: BusData? = null

//    fun updateLocation(newData: LatLng) { busLocation.value = Resource.Success(newData) }
        //https://habr.com/ru/post/432310/

    fun startWebSocket() = flow<LatLng>
    {
            client.webSocket(
                method = HttpMethod.Get,
                host = HOST,
                port = PORT,
                path = PATH
            ) {
                    for (response in incoming)
                    {
                        emit(response.readBytes() as LatLng) // !_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_

                    }
            }

    }
//            client.close()
//            println("Connection closed. Goodbye!")

/*    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> fromByteArray(byteArray: ByteArray): T {
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInput: ObjectInput
        objectInput = ObjectInputStream(byteArrayInputStream)
        val result = objectInput.readObject() as T
        objectInput.close()
        byteArrayInputStream.close()
        return result
    }*/

}