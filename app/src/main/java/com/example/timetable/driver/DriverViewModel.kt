package com.example.timetable.driver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.example.timetable.EndPoint
import com.example.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get


class DriverViewModel(application : Application): AndroidViewModel(application)
{
    private val HOST = EndPoint.host
    private val urlFlightNames = EndPoint.protocol + HOST + EndPoint.routes_names_id

    var routesInf = arrayOf<FlightsNameResponse>()

    val clientRoutes = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }

    suspend fun getFlight(): List<FlightsNameResponse>? =
        try {
            val response: List<FlightsNameResponse> = clientRoutes.get(urlFlightNames)
            routesInf = response.toTypedArray()
            response
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}