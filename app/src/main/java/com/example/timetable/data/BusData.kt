package com.example.timetable.data

import com.google.firebase.firestore.GeoPoint

data class BusData(
    var name: String?=null,
//    var uid: String,
    val route: MutableList<GeoPoint>?=null,
    val busStops: MutableList<BusStop>?=null,
)
