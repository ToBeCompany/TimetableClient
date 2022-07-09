package com.dru128.timetable.data

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.dru128.timetable.data.metadata.Route
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class JsonDataManager(var context: Context): DataManager
{
    private val TAG_ROUTES: String = "TAG_ROUTES"
    private val TAG_FAVOURITE_ROUTES: String = "TAG_FAVOURITE_ROUTES"
    private val TAG_DB_VERSION: String = "TAG_DB_VERSION"
    private val TAG_IS_INIT_DB: String = "TAG_IS_INIT_DB"

    private var preference = PreferenceManager.getDefaultSharedPreferences(context)

    override fun loadRoutes(): Array<Route>?
    {
        val jsonRoutes = preference.getString(TAG_ROUTES, null)
        return if (jsonRoutes.isNullOrEmpty()) null
        else Json.decodeFromString(jsonRoutes)
    }


    override fun saveRoutes(routes: Array<Route>, db_version: Int)
    {
        preference
            .edit()
            .putString(TAG_ROUTES, Json.encodeToString(routes))
            .putInt(TAG_DB_VERSION, db_version)
            .putBoolean(TAG_IS_INIT_DB, true)
            .apply()
    }

    override fun clearStorage()
    {
        preference
            .edit()
            .clear()
            .apply()
    }

    override fun getDBVersion(): Int? =
        if (preference.getBoolean(TAG_IS_INIT_DB, false))
            preference.getInt(TAG_DB_VERSION, Int.MIN_VALUE)
        else
            null
    // ---------------------------------------------------------------------------------------------
    override fun loadFavouriteRoutes(): Array<String>
    {
        return Json.decodeFromString<Array<String>>(
            preference.getString(TAG_FAVOURITE_ROUTES, "[]") ?: "[]"
        )
    }


    override fun deleteFromFavouriteRoutes(id: String) {
        val newFavourites = loadFavouriteRoutes()
            .filter { it != id }
            .toTypedArray()

        preference
            .edit()
            .putString(
                TAG_FAVOURITE_ROUTES,
                Json.encodeToString(newFavourites)
            )
            .apply()
    }

    override fun addToFavouriteRoutes(id: String) {
        var newFavourites = loadFavouriteRoutes()
        newFavourites += id
        Log.d("event", "addToFavouriteRoutes, $newFavourites")

        preference
            .edit()
            .putString(
                TAG_FAVOURITE_ROUTES,
                Json.encodeToString(newFavourites)
            )
            .apply()
    }
    // ---------------------------------------------------------------------------------------------

}