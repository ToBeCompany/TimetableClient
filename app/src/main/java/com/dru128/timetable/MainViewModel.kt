package com.dru128.timetable

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.data.metadata.User
import io.ktor.client.call.body
import io.ktor.client.request.get


class MainViewModel(application : Application): AndroidViewModel(application)
{
    suspend fun getUser(id: String): User? =
        try {
            Repository.client.get(EndPoint.auth + id).body<User>()
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}