package com.example.timetable

import com.example.timetable.data.Flight
import com.example.timetable.data.response.FlightsNameResponse

object Storage
{
    var flights: List<Flight> = listOf()

    var flightsNames: List<FlightsNameResponse> = listOf()

}