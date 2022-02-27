package com.dru128.timetable.data

import com.dru128.timetable.data.metadata.Route

interface DataManager
{
    fun loadRoutes(): Array<Route>?

    fun saveRoutes(routes: Array<Route>)

    fun clearStorage()
}