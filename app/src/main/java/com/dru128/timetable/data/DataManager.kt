package com.dru128.timetable.data

import com.dru128.timetable.data.metadata.Route

interface DataManager
{
    fun loadRoutes(): Array<Route>?
    fun saveRoutes(routes: Array<Route>, db_version: Int)

    fun getDBVersion(): Int?

    fun clearStorage()

    fun loadFavouriteRoutes() : Array<String>
    fun deleteFromFavouriteRoutes(id: String)
    fun addToFavouriteRoutes(id: String)
}