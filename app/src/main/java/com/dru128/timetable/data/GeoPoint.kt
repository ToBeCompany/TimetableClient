package com.dru128.timetable.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "GeoPoint")
data class GeoPoint(
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