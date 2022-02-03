package com.dru128.timetable.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Entity(tableName = "Route2")
data class RouteComplex( // маршрут
    @PrimaryKey
    var id: String,
    @ColumnInfo(name = "name")
    var name: String = "",
//    @ColumnInfo(name = "pointList")
//    var points: List<RoutePoint> = listOf(),
//    @ColumnInfo(name = "busStopList")
//    var busStopsWithTime: List<BusStopWithTimeEntity> = listOf()
)
