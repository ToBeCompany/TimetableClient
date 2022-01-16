package com.example.timetable.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.data.metadata.User
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*


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