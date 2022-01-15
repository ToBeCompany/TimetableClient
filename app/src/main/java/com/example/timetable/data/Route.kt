package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.timetable.data.database.ObjectBusStopWithTimeTypeConvert
import com.example.timetable.data.database.ObjectPointsTypeConvert
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "Route")
data class Route( // маршрут
    @SerialName("id")
        @PrimaryKey
            var id: String = "",
    @SerialName("idm_Foreign")
        @ColumnInfo(name = "name")
            var name: String = "",
    @SerialName("lineMarshtriectori")
        @ColumnInfo(name = "pointList")
        @field:TypeConverters(ObjectPointsTypeConvert::class)
            var points: List<GeoPosition> = listOf(),
    @SerialName("idOst")
        @ColumnInfo(name = "busStopWithTimeList")
        @field:TypeConverters(ObjectBusStopWithTimeTypeConvert::class)
    var busStopsWithTime: List<BusStopWithTime> = listOf()
)