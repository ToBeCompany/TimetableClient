package com.dru128.timetable.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.data.room.DataManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class JsonDataManager(var context: Context): DataManager
{
    private val TAG_ROUTES: String = "TAG_ROUTES"
    private var preference = PreferenceManager.getDefaultSharedPreferences(context)

    override fun loadRoutes(): Array<Route>?
    {
        val jsonRoutes = preference.getString(TAG_ROUTES, null)
        return if (jsonRoutes.isNullOrEmpty()) null
        else Json.decodeFromString(jsonRoutes)
    }


    override fun saveRoutes(routes: Array<Route>)
    {
        preference
            .edit()
            .putString(TAG_ROUTES, Json.encodeToString(routes))
            .apply()
    }

    override fun clearStorage()
    {
        preference
            .edit()
            .putString(TAG_ROUTES, "")
            .apply()
    }
}