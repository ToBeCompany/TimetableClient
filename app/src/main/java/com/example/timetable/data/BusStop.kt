package com.example.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "BusStop")
data class BusStop( // остановка
        @PrimaryKey
            var id: String = "",
    @SerialName("name")
        @ColumnInfo(name = "name")
            var name: String = "",
    @SerialName("geopos")
        @ColumnInfo(name = "position")
            var position: GeoPosition? = null,
)
