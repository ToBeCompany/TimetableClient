package com.example.timetable.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class BusStopTime(
    var id: String? = null,
    @SerialName("busStop") var busStop: BusStop? = null,
    @SerialName("geopos") var time: String? = null
)