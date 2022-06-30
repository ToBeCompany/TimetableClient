package com.dru128.timetable.driver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.App
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import dru128.timetable.R
import com.dru128.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.request.get


class DriverViewModel(application : Application): AndroidViewModel(application)
{
    var routesInf = arrayOf<FlightsNameResponse>()

    suspend fun getFlight(): Array<FlightsNameResponse>? =
        try {
            val response: List<FlightsNameResponse> = Repository.client.get(EndPoint.routes_names_id)
            routesInf = arrayOf()
            routesInf += FlightsNameResponse( App.globalContext.getString(R.string.route_not_selected), "" )
            routesInf += response
            routesInf
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}