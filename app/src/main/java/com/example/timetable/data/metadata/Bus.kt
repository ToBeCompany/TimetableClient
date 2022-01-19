package com.example.timetable.data.metadata

import kotlinx.serialization.Serializable


@Serializable
data class Bus( // автобус
    var name: String,
    var id: String,
    var point: GeoPoint? = null
)
