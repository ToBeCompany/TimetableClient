package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusStop( // остановка
    @SerialName("id")
    var id: String? = null,
    var busStopWithTime_id: Int? = null,
    @SerialName("name")
    var name: String = "",
    @SerialName("geopos")
    var point: GeoPoint? = null,
)
