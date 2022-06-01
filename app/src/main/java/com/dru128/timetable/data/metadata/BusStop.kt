package com.dru128.timetable.data.metadata

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusStop( // остановка
    @SerialName("id")
    var id: String = "",
    @SerialName("name")
    var name: String = "",
    @SerialName("geopos")
    var position: GeoPosition,
)
