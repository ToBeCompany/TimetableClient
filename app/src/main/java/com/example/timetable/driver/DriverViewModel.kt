package com.example.timetable.driver

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.Resource
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.timetable.App
import com.example.timetable.data.n.GeoPosition
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.security.Security


class DriverViewModel(application : Application): AndroidViewModel(application)
{
    private val locationManager by lazy {
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val busLocation = MutableSharedFlow<GeoPosition>()
    private var id = "1"
    private val HOST = "fierce-woodland-54822.herokuapp.com"
    private val PATH = "/driver/$id"

    private val listener = LocationListener { // тут данные меняются
        var position = GeoPosition(it.latitude, it.longitude)
        Log.d("LocationListener", position.toString())
        viewModelScope.launch {
            busLocation.emit(position)
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

    @SuppressLint("MissingPermission")
    suspend fun startSearch()
    {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1_000,
                1f,
                listener
            )

//        startWebSocket()
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
                Log.d("pushLocation", it.toString())
                withContext(Dispatchers.IO) {
                    send(Frame.Text(it.toString()))
                }
            }
        }
    }

    fun clearSearch() {
        locationManager.removeUpdates(listener)
    }
}