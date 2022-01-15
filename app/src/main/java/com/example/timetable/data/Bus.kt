package com.example.timetable.data

import kotlinx.serialization.Serializable


@Serializable
data class Bus( // автобус
    var name: String,
    var id: String,
    var position: GeoPosition? = null
)
