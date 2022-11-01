package com.dru128.timetable.admin.map.dispacher

import com.dru128.timetable.data.metadata.GeoPosition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BusLocationResponse(
    var id: String = "",
    var position: GeoPosition? = GeoPosition()
)
