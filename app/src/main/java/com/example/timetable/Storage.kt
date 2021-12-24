package com.example.timetable

import com.example.timetable.data.Flight
import com.example.timetable.data.response.FlightsNameResponse

object Storage
{
    var flights: MutableList<Flight> = mutableListOf()

    var flightsNames: MutableList<FlightsNameResponse> = mutableListOf()

}