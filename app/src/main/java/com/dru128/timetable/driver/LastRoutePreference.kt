package com.dru128.timetable.driver

import android.content.Context
import androidx.preference.PreferenceManager

class LastRoutePreference(var context: Context)
{
    private val TAG_ROUTE_ID: String = "LAST_ROUTE_ID"
    private var preference = PreferenceManager.getDefaultSharedPreferences(context)

    fun setRouteId(route_id: String)
    {
        preference
            .edit()
            .putString(TAG_ROUTE_ID, route_id)
            .apply()
    }

    fun getRouteId(): String
        = preference.getString(TAG_ROUTE_ID, null).toString()
}