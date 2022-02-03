package com.dru128.timetable.data.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class BusStopWithTime(
    @SerialName("first")
    var busStop: BusStop? = null,
    @SerialName("second")
    var time: String = ""
)