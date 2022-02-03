package com.dru128.timetable.data.room

import com.dru128.timetable.App

class RoomDataManager(app: App)//: DataManager
{
    private val routeDao: RouteDao = app.database.routeDao()

//    override fun loadRoutes(): Array<Route> {
//        TODO("Not yet implemented")
//    }
//
//    override fun saveRoutes(routes: Array<Route>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun clearStorage() {
//        TODO("Not yet implemented")
//    }
}