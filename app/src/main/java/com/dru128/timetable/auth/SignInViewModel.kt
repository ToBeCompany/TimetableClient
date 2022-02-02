package com.dru128.timetable.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get


class SignInViewModel(application : Application): AndroidViewModel(application)
{
    suspend fun getUser(id: String): User? =
        try {
            Storage.client.get(EndPoint.auth + id)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}