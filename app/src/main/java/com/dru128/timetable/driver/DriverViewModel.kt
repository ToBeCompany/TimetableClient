package com.dru128.timetable.driver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.App
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import dru128.timetable.R
import com.dru128.timetable.data.metadata.response.RouteNamesResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse


class DriverViewModel(application : Application): AndroidViewModel(application)
{
    var routesInf = arrayOf<RouteNamesResponse>()

    suspend fun getRouteNames(): Array<RouteNamesResponse>? =
        try {
            val response: HttpResponse = Repository.client.get(EndPoint.routes_names_id)
            Log.d("Server", "Status code: ${response.status.value}")

            val routeNames = response.body<List<RouteNamesResponse>>()

            routesInf = arrayOf()
            routesInf += RouteNamesResponse( App.globalContext.getString(R.string.route_not_selected), "" )
            routesInf += routeNames
            routesInf
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}