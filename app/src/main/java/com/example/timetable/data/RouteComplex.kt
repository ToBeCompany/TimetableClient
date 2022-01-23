package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "Route")
data class RouteComplex( // маршрут
    @SerialName("id")
    @PrimaryKey
    var id: String,
    @SerialName("idm_Foreign")
    @ColumnInfo(name = "name")
    var name: String = "",
    @SerialName("lineMarshtriectori")
    @ColumnInfo(name = "pointList")
    var points: List<RoutePoint> = listOf(),
    @SerialName("idOst")
    var busStopsWithTime: List<BusStopWithTime> = listOf()
)
