package com.dru128.timetable.data.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GeoPosition(
    @SerialName("lat")
    var latitude: Double = 0.0,
    @SerialName("long")
    var longitude: Double = 0.0
)