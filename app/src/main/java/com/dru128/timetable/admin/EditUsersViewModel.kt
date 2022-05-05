package com.dru128.timetable.admin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.User
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditUsersViewModel(application : Application): AndroidViewModel(application)
{
    suspend fun createUser(user: User): String? =
        try {
            Log.d("Server", "SUCCESS")
            Storage.client.post(EndPoint.createUser + user) {
                this.body = Json.encodeToString(user)
            }
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }

    suspend fun deleteUser(id: String): String? =
        try {
            Log.d("Server", "SUCCESS")
            Storage.client.get(EndPoint.deleteUser + id)
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }
}