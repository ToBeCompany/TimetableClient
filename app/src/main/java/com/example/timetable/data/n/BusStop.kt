package com.example.timetable.data.n

import com.google.android.gms.maps.model.LatLng

data class BusStop(
    var name: String,
    var id: String,
    var position: LatLng,
    var time: String? = null
)
