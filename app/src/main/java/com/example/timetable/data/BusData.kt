package com.example.timetable.data

import com.google.firebase.firestore.GeoPoint


data class BusData(
    var name: String,
//    var uid: String,
    val route: MutableList<GeoPoint>,
    val busStops: MutableList<BusStop>,
)
