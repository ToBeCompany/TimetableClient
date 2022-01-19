package com.example.timetable.data.database

import androidx.room.TypeConverter
import com.example.timetable.data.BusStopWithTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ObjectBusStopWithTimeTypeConvert
{
    @TypeConverter
    fun fromList(value: List<BusStopWithTime>): String?
    {
        return if (!value.isNullOrEmpty())
            Json.encodeToString(value)
        else
            null
    }

    @TypeConverter
    fun toList(value: String?): List<BusStopWithTime>?
    {
        if (value.isNullOrEmpty()) return null
        return Json.decodeFromString(value) as List<BusStopWithTime>
    }
}