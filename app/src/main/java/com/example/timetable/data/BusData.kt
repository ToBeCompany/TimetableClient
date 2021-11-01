package com.example.timetable.data


data class BusData(
    var name: String,
    var id: String,
//    val route: MutableList<>,
    val busStops: MutableList<BusStop>,

)
