package com.dru128.timetable.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "BusStop")
data class BusStopEntity( // остановка
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "busStopWithTime_id")
    var busStopWithTime_id: Int,
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "position")
    var point: BusStopPoint? = null,
)
