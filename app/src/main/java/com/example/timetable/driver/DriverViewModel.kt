package com.example.timetable.driver

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.n.GeoPosition
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.Security


class DriverViewModel(application : Application): AndroidViewModel(application)
{

    private val busLocation = MutableSharedFlow<GeoPosition>()
    private var id = "1"
    private val HOST = "fierce-woodland-54822.herokuapp.com"
    private val PATH = "/driver/$id"

    @SuppressLint("MissingPermission")
    fun startSearch(context: Context) // это нужно запустить в самом начале работы программы
    {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000, 10f,
            listener
        ) // здесь можно указать другие более подходящие вам параметры
        viewModelScope.launch {
            startWebSocket()
        }
    }

    private var listener = LocationListener {
        val position = GeoPosition(it.latitude, it.longitude)
        viewModelScope.launch {
            busLocation.emit(position)
        }
    }
    suspend fun startWebSocket()
    {
        val client = providerKtorCLient().webSocket(
            method = HttpMethod.Get,
            host = HOST,
            path = PATH
        )
        {

            busLocation.collect {
                Log.d("newLocation(driver)", it.latitude.toString() + it.longitude.toString())
                withContext(Dispatchers.IO) {
                    send(
                        Frame.Text(
                            Json.encodeToString(it)
                        )
                    )
                }
            }
        }
    }

    fun providerKtorCLient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }
}