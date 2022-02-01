package com.example.timetable.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.timetable.data.metadata.User
import com.example.timetable.worker.Storage
import com.example.timetable.worker.Storage.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get


class SignInViewModel: ViewModel() {
    suspend fun getUser(id: String): User? =
        try {
            Storage.client.get("$BASE_URL/sign/$id")
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }
}