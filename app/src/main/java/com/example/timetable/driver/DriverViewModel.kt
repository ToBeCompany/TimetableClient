package com.example.timetable.driver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.EndPoint
import com.example.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*


class DriverViewModel(application : Application): AndroidViewModel(application)
{
    private val HOST = EndPoint.host
    private val urlFlightNames = EndPoint.protocol + HOST + EndPoint.routes_names_id

    val clientRoutes = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }

    suspend fun getFlight(): List<FlightsNameResponse>? =
        try {
            clientRoutes.get(urlFlightNames)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}