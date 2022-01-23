package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class BusStopWithTime(
    @SerialName("id")
    var id: Int? = null,
    var route_id: String = "",
    @SerialName("first")
    var busStop: BusStop? = null,
    @SerialName("second")
    var time: String = ""
)