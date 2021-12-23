package com.example.timetable.data.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch

class SignInViewModel(application : Application): AndroidViewModel(application)
{
    private val url = "fierce-woodland-54822.herokuapp.com/signin/"
    val client = HttpClient(CIO) {
        install(JsonFeature)
    }

    suspend fun authUserOnServer(id: String): User =
        client.get(url + id)

}