package com.example.timetable.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.data.metadata.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get


class SignInViewModel(application : Application): AndroidViewModel(application)
{
    private val url = "https://fierce-woodland-54822.herokuapp.com/sign/"
    val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }

    suspend fun getUser(id: String): User? =
        try {
            client.get(url + id)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }

}