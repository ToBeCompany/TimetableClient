package com.example.timetable.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FlightsNameResponse(
    var name: String? = "",
    var id: String? = ""
)