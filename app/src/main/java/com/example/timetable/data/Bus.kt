package com.example.timetable.data

import com.google.android.gms.maps.model.LatLng

data class Bus(
    var name: String,
    var id: String,
    var position: LatLng
)
