package com.example.timetable.data

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parcelize

data class BusData(
    var name: String?=null,
    val route: MutableList<GeoPoint>?=null,
    val busStops: MutableList<BusStop>?=null,
)