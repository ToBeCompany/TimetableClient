package com.example.timetable.database

import android.location.Location
import android.util.Log
import com.example.timetable.Resource
import com.example.timetable.data.n.Bus
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.get
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
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



//                _tapRequestState.value = Resource.Success(data.data as? LatLng ?: LatLng(100.0, 0.0))

    fun updateData()
    {
        location.value = Resource.Loading()

    }


//    private fun toObject(stringValue: String): Field {
//        return JSON.parse(Field.serializer(), stringValue)
//    }
}