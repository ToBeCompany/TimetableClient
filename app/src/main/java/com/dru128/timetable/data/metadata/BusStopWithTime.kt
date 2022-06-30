package com.dru128.timetable.data.metadata

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusStopWithTime(
    @SerialName("first")
    var busStop: BusStop,
    @SerialName("second")
    var time: String = ""
)