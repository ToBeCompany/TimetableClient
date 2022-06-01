package com.dru128.timetable.data.metadata

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Route( // маршрут
    @SerialName("id")
    var id: String = "",
    @SerialName("idm_Foreign")
    var name: String = "",
    @SerialName("lineMarshtriectori")
    var positions: List<GeoPosition> = listOf(),
    @SerialName("idOst")
    var busStopsWithTime: List<BusStopWithTime> = listOf()
)