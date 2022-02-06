package com.dru128.timetable.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "BusStopPoint")
data class BusStopPoint(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "busStop_id")
    var busStop_id: Int,
    @SerialName("lat")
    @ColumnInfo(name = "latitude")
    var latitude: Double = 0.0,
    @SerialName("long")
    @ColumnInfo(name = "longitude")
    var longitude: Double = 0.0
)