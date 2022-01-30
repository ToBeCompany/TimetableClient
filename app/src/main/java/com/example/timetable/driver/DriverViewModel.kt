package com.example.timetable.driver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.App
import com.example.timetable.EndPoint
import com.example.timetable.R
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

    suspend fun getFlight(): Array<FlightsNameResponse>? =
        try {
            val response: List<FlightsNameResponse> = clientRoutes.get(urlFlightNames)
            routesInf += FlightsNameResponse( App.globalContext.getString(R.string.route_not_selected), "" )
            routesInf += response
            routesInf
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}