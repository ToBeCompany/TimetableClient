package com.example.timetable.worker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.data.response.FlightsNameResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*

class RouteViewModel(application : Application): AndroidViewModel(application)
{
    private var url = "fierce-woodland-54822.herokuapp.com/flight"


    val client = HttpClient(CIO) {

    }


    suspend fun getFlight(): List<FlightsNameResponse> = client.get(url)

}