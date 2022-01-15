package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "BusStopTime")
class BusStopWithTime(
    @SerialName("id")
        @PrimaryKey
            var id: String = "",
    @SerialName("first")
        @ColumnInfo(name = "busStop")
            var busStop: BusStop? = null,
    @SerialName("second")
        @ColumnInfo(name = "time")
            var time: String = ""
)