package com.example.timetable.data.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class BusStopWithTime(
    @SerialName("id")
    var id: String = "",
    @SerialName("first")
    var busStop: BusStop? = null,
    @SerialName("second")
    var time: String = ""
)