package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.timetable.data.database.ObjectBusStopWithTimeTypeConvert
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