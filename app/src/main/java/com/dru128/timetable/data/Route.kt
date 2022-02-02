package com.dru128.timetable.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
//@Entity(tableName = "Route")
data class Route( // маршрут
    @SerialName("id")
//    @PrimaryKey
    var id: String = "",
    @SerialName("idm_Foreign")
//    @ColumnInfo(name = "name")
    var name: String = "",
    @SerialName("lineMarshtriectori")
//    @ColumnInfo(name = "pointList")
    var points: List<GeoPoint> = listOf(),
    @SerialName("idOst")
//    @ColumnInfo(name = "busStopWithTimeList")
    var busStopsWithTime: List<BusStopWithTime> = listOf()
)