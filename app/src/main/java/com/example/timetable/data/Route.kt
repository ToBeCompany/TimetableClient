package com.example.timetable.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Route( // маршрут
    @SerialName("idm_Foreign") var name: String? = null,
    var id: String? = null,
    @SerialName("lineMarshtriectori") var points: List<GeoPosition>? = null,
    @SerialName("idOst") var busStops: List<BusStopTime>? = null
)