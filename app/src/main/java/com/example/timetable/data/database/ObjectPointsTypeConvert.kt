package com.example.timetable.data.database

import androidx.room.TypeConverter
import com.example.timetable.data.GeoPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ObjectPointsTypeConvert
{
    @TypeConverter
    fun fromList(value: List<GeoPoint>): String?
    {
        return if (!value.isNullOrEmpty())
            Json.encodeToString(value)
        else
            null
    }

    @TypeConverter
    fun toList(value: String?): List<GeoPoint>?
    {
        if (value.isNullOrEmpty()) return null
        return Json.decodeFromString(value) as List<GeoPoint>
    }
}
