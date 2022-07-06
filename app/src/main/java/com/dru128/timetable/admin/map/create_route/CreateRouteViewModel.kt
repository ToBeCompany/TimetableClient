package com.dru128.timetable.admin.map.create_route

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.admin.edituser.UsersStorage
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.data.metadata.User
import com.mapbox.android.gestures.StandardGestureDetector
import com.mapbox.geojson.Point
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

class CreateRouteViewModel : ViewModel()
{
    var points = mutableListOf<Point>()

    var isInitRoute = false
    var route: Route = Route()

    suspend fun createRoute(route: Route): Boolean
    {
        try {
            val response: HttpResponse = Repository.client.post(EndPoint.createRoute) {
                this.setBody(Json.encodeToJsonElement(route))
            }
            Log.d("Server", "Status code: ${response.status.value}")

            if (response.status.value == 200)
            {
                RouteAdminStorage.routes += route
                Log.d("Server", "SUCCESS")
                return true
            } else
                return false
        } catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }
    suspend fun editRoute(route: Route): Boolean
    {
        try {
            val response: HttpResponse = Repository.client.post(EndPoint.editRoute) {
                this.setBody(Json.encodeToJsonElement(route))
            }
            Log.d("Server", "Status code: ${response.status.value}")

            if (response.status.value == 200)
            {
                for (i in RouteAdminStorage.routes.indices)
                    if (RouteAdminStorage.routes[i].id == route.id)
                        RouteAdminStorage.routes[i] = route
                Log.d("Server", "SUCCESS")
                return true
            } else
                return false
        } catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }
}