package com.example.timetable.database

import com.example.timetable.Resource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow

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