package com.dru128.timetable.data.metadata

import kotlinx.serialization.Serializable


@Serializable
data class Bus( // автобус
    var id: String = "",
    var name: String = "",
    var position: GeoPosition? = null
)
