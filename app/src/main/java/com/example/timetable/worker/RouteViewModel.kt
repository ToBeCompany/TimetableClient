package com.example.timetable.worker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.EndPoint
import com.example.timetable.data.response.FlightsNameResponse
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*


class RouteViewModel(application : Application): AndroidViewModel(application)
{
    private val urlFlightNames = EndPoint.protocol + EndPoint.host + EndPoint.routes_names_id


    val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }


    suspend fun getFlight(): List<FlightsNameResponse>? =
        try {
            client.get(urlFlightNames)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}