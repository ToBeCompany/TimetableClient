package com.example.timetable.database

import android.location.Location
import com.example.timetable.Resource
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WebSocketTracker
{
    private val location: MutableStateFlow<Resource<LatLng>?> = MutableStateFlow(null)


    private val PORT = 8080
    private val HOST = "127.0.0.1"
    private val PATH = "/chat"

    val client = HttpClient {
        install(WebSockets)
    }

    fun startWebSocket()
    {
        runBlocking {
            client.webSocket(
                method = HttpMethod.Get,
                host = HOST,
                port = PORT,
                path = PATH
            ) {
                val OutputRoutine = launch { outputMessages() }
//                val userInputRoutine = launch { inputMessages() }

//                userInputRoutine.join() // Wait for completion; either "exit" or error
                OutputRoutine.cancelAndJoin()
            }
        }
        client.close()
        println("Connection closed. Goodbye!")
    }



    private suspend fun DefaultClientWebSocketSession.outputMessages()
    {

        for (data in incoming)
        {
            if (data is Frame.Binary)
            {
                var location = data.readBytes() as Location
            }
        }
    }
//                _tapRequestState.value = Resource.Success(data.data as? LatLng ?: LatLng(100.0, 0.0))

    fun updateData()
    {
        location.value = Resource.Loading()

    }


//    private fun toObject(stringValue: String): Field {
//        return JSON.parse(Field.serializer(), stringValue)
//    }
}