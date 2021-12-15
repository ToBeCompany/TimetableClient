package com.example.timetable.data.n

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable


@Serializable
class GeoPosition(var latitude: Double, var longitude: Double)
{
    fun ToLatLng() = LatLng(latitude, longitude)
}