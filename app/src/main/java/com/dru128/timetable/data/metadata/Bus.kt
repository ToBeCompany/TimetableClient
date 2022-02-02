package com.dru128.timetable.data.metadata

import kotlinx.serialization.Serializable
import com.dru128.timetable.data.GeoPoint


@Serializable
data class Bus( // автобус
    var id: String = "",
    var name: String = "",
    var point: GeoPoint? = null
)
