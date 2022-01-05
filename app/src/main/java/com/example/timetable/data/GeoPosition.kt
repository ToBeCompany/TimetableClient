package com.example.timetable.data

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GeoPosition(
    @SerialName("lat") var latitude: Double = 0.0,
    @SerialName("long") var longitude: Double = 0.0
)
{
    fun toLatLng() = LatLng(latitude, longitude)
}