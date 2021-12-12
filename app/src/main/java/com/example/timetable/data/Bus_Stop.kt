package com.example.timetable.data

import com.google.firebase.firestore.GeoPoint
import kotlinx.serialization.Serializable

data class Bus_Stop(
    var name: String?=null,
    var time: String?=null,
    var position: GeoPoint?=null
)