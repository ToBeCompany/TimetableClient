package com.example.timetable.data

import com.google.firebase.firestore.GeoPoint

data class BusData(
    var name: String?=null,
    val route: MutableList<GeoPoint>?=null,
    val busStops: MutableList<Bus_Stop>?=null,
)