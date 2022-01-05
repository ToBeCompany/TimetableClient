package com.example.timetable.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusStop( // остановка
    @SerialName("name") var name: String? = null,
    var id: String? = null,
    @SerialName("geopos") var position: GeoPosition? = null,
    var time: String? = null
)
