package com.dru128.timetable.admin.map.create_route

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.IDManager
import com.dru128.timetable.tools.Resource
import com.mapbox.geojson.Point
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement


class CreateRouteViewModel : ViewModel()
{
    private val _routePoints: MutableStateFlow<List<Point>> = MutableStateFlow(listOf<Point>())
    val routePoints: StateFlow<List<Point>> = _routePoints

    private val _busStops: MutableStateFlow<List<BusStopWithTime>> = MutableStateFlow(listOf<BusStopWithTime>())
    val busStops: StateFlow<List<BusStopWithTime>> = _busStops

    var routeName: String = ""
    var routeId: String = ""

    fun setBusStops(newBusStops: List<BusStopWithTime>)
    {
        _busStops.value = newBusStops
    }

    fun addBusStop(_BusStop: BusStopWithTime)
    {
        _busStops.value += _BusStop
    }

    fun changeBusStop(id: String, name: String, time: String)
    {
        Log.d("action", "changeBusStop")

        for (curBusStop in _busStops.value)
            if (curBusStop.busStop.id == id)
            {
                curBusStop.time = time
                curBusStop.busStop.name = name
            }
    }

    fun deleteBusStop(deleteBusStop: BusStopWithTime) { deleteBusStop(deleteBusStop.busStop.id) }
    fun deleteBusStop(id: String)
    {
        val newBusStops = mutableListOf<BusStopWithTime>()
        for (curBusStop in _busStops.value)
            if (curBusStop.busStop.id != id)
                newBusStops.add(curBusStop)
            else
                Log.d("deletedBusStop", "id = " + curBusStop.busStop.id)

        _busStops.value = newBusStops
    }

    fun addRouteDot(point: Point)
    {
        if (_routePoints.value.isNotEmpty() &&
            calcDistance(point, _routePoints.value.first()) <
            calcDistance(point, _routePoints.value.last()))
                _routePoints.value = listOf(point) + _routePoints.value
        else
            _routePoints.value = _routePoints.value + point
    }
    fun setRouteDots(points: List<Point>)
    {
        _routePoints.value = points
    }
    fun deleteRouteDot(point: Point)
    {
        val newPoints = mutableListOf<Point>()
        for (_point in _routePoints.value)
            if (_point.latitude() != point.latitude() || _point.longitude() != point.longitude())
                newPoints.add(_point)

        _routePoints.value = newPoints
    }

    suspend fun createRoute(): Boolean
    {
        return true
//        val route = Route(
//            id = /*(1..238651).random().toString(),*/IDManager.generateID(),
//            name = routeName,
//            positions = pointToGeoPos(_routePoints.value),
//            busStopsWithTime = _busStops.value
//        )
//
//        val response: HttpResponse
//        try {
//            response = Repository.client.post(EndPoint.createRoute) {
//                this.setBody(Json.encodeToJsonElement(route))
//            }
//        } catch (error: Exception) {
//            Log.d("Server", "ERROR: ${error.message}")
//            return false
//        }
//        Log.d("Server", "Status code: ${response.status.value}")
//
//        return if (response.status.value == 200)
//        {
//            RouteAdminStorage.routes.value.data?.let { routes ->
//                val newRoutes = routes.plus(route)
//                RouteAdminStorage.routes.value = Resource.Success(newRoutes)
//            }
//            Log.d("Server", "SUCCESS")
//            true
//        } else
//            false
    }
    suspend fun editRoute(): Boolean
    {
        return true
//        val route = Route(
//            id = routeId,
//            name = routeName,
//            positions = pointToGeoPos(_routePoints.value),
//            busStopsWithTime = _busStops.value
//        )
//
//        val response: HttpResponse
//        try {
//            response = Repository.client.post(EndPoint.editRoute) {
//                this.setBody(Json.encodeToJsonElement(route))
//            }
//        } catch (error: Exception) {
//            Log.d("Server", "ERROR: ${error.message}")
//            return false
//        }
//        Log.d("Server", "Status code: ${response.status.value}")
//
//        return if (response.status.value == 200)
//        {
//            RouteAdminStorage.routes.value.data?.let { _routes ->
//                val newRoutes = Array<Route> ( _routes.size) { i ->
//                    if (_routes[i].id == route.id) route
//                    else _routes[i]
//                }
//                RouteAdminStorage.routes.value = Resource.Success(newRoutes)
//
//            }
//            Log.d("Server", "SUCCESS")
//            true
//        } else
//            false
    }

    fun calcDistance(point1: Point, point2: Point): Float // расстояние между точками
    {
        return Location("p1").apply {
            latitude = point1.latitude()
            longitude = point1.longitude()
        }.distanceTo(
            Location("p2").apply {
                latitude = point2.latitude()
                longitude = point2.longitude()
            }
        )
    }

    private fun pointToGeoPos(points: List<Point>): List<GeoPosition> =
        List<GeoPosition>(points.size) { i ->
            GeoPosition(latitude = points[i].latitude(), longitude = points[i].longitude())
        }
}