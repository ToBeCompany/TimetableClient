package com.example.timetable.data

import com.google.firebase.firestore.GeoPoint

data class BusStop(
    var name: String?=null,
    var time: String?=null,
    var position: GeoPoint?=null
)