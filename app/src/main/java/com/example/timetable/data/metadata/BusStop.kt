package com.example.timetable.data.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusStop( // остановка
    var id: String = "",
    @SerialName("name")
    var name: String = "",
    @SerialName("geopos")
    var position: GeoPosition? = null,
)
