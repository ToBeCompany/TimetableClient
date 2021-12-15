package com.example.timetable.driver

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import android.widget.Toast
import com.example.timetable.App
import com.example.timetable.data.n.GeoPosition
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.security.Security


class DriverViewModel(application : Application): AndroidViewModel(application)
{
    private val locationManager by lazy {
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val busLocation = flow<GeoPosition> {}
    private var id = "1"
    private val HOST = "fierce-woodland-54822.herokuapp.com"
    private val PATH = "/driver/$id"

    private val listener = LocationListener { // тут данные меняются
        Toast.makeText(App.globalContext, it.toString(), Toast.LENGTH_SHORT).show()
        var position = GeoPosition(it.latitude, it.longitude)
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
        startWebSocket()
        val lastPositionNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5_000,
                10f,
                listener
            )

    }

    suspend fun startWebSocket()
    {
        val client = providerKtorCLient().webSocket(
            method = HttpMethod.Get,
            host = HOST,
            path = PATH
        )
        {


            coroutineScope {
                while (true)
                {
                    delay(3_000)
                    send(Frame.Text("it.toString()"))
                }
            }
//            busLocation.collect {
//                send(Frame.Text(it.toString()))
//            }
        }
    }

    fun clearSearch() {
        locationManager.removeUpdates(listener)
    }
}