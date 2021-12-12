package com.example.timetable.data.n

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable

data class Bus(
    var name: String,
    var id: String,
    var position: LatLng
)
