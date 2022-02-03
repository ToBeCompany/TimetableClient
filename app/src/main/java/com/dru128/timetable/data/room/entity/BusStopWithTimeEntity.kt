package com.dru128.timetable.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "BusStopWithTime")
class BusStopWithTimeEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    @ColumnInfo(name = "route_id")
    var route_id: String = "",
    @ColumnInfo(name = "busStop")
    var busStop: BusStopEntity? = null,
    @ColumnInfo(name = "time")
    var time: String = ""
)