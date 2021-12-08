package com.example.timetable.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.Resource
import com.example.timetable.data.BusData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow

class MapViewModel(application : Application): AndroidViewModel(application)
{
    val busLocation: MutableStateFlow<Resource<LatLng>?> = MutableStateFlow(null)
    var busData: BusData? = null

    fun updateLocation(newData: LatLng) { busLocation.value = Resource.Success(newData) }


}