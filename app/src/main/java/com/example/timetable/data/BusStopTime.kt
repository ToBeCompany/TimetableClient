package com.example.timetable.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class BusStopTime(
//    var id: String? = null,
    @SerialName("first") var busStop: BusStop? = null,
    @SerialName("second") var time: String? = null
)