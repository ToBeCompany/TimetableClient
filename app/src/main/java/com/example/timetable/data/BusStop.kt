package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "BusStop")
data class BusStop( // остановка
    @PrimaryKey()
    @SerialName("id")
    var id: String? = null,
    @ColumnInfo(name = "busstopwithtime_id")
    var busStopWithTime_id: Int? = null,
    @SerialName("name")
    @ColumnInfo(name = "name")
    var name: String = "",
    @SerialName("geopos")
    @ColumnInfo(name = "position")
    var point: GeoPoint? = null,
)
