package com.dru128.timetable.data.metadata.response

import kotlinx.serialization.Serializable


@Serializable
data class FlightsNameResponse(
    var name: String? = "",
    var id: String? = ""
)