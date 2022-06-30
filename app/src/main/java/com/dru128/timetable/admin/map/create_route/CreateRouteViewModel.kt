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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CreateRouteViewModel : ViewModel()
{
    var points = mutableListOf<Point>()

    var isInitRoute = false
    var route: Route = Route()



    suspend fun createRoute(route: Route): Boolean =
        try {
            Log.d("Server", "SUCCESS")
            val response: Boolean? = Repository.client.post(EndPoint.createRoute) {
                this.body = Json.encodeToString(route)
            }
            Log.d("createRoute", "ответ = $response")


            RouteAdminStorage.routes.value += route
            true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            false
        }

    suspend fun editRoute(route: Route): Boolean =
        try {
            Log.d("Server", "SUCCESS")
            val response: Boolean? = Repository.client.post(EndPoint.editRoute) {
                this.body = Json.encodeToString(route)
            }

            for (i in RouteAdminStorage.routes.value.indices)
                if (RouteAdminStorage.routes.value[i].id == route.id)
                    RouteAdminStorage.routes.value[i] = route

            true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            false
        }
}