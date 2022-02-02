package com.dru128.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "GeoPoint")
data class BusStopPoint(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "busstop_id")
    var busStop_id: Int = 0,
    @SerialName("lat")
    @ColumnInfo(name = "latitude")
    var latitude: Double = 0.0,
    @SerialName("long")
    @ColumnInfo(name = "longitude")
    var longitude: Double = 0.0
)
{
    fun toLatLng() = LatLng(latitude, longitude)
}