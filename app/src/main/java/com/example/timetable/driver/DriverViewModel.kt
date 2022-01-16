package com.example.timetable.driver

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetable.EndPoint
import com.example.timetable.data.metadata.GeoPosition
import com.example.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
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
    private val HOST = EndPoint.host
    private val urlFlightNames = EndPoint.protocol + HOST + EndPoint.routes_names_id

    val clientRoutes = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }
    var webSocketSession: DefaultClientWebSocketSession? = null

    @SuppressLint("MissingPermission")
    fun startSearch(context: Context, trackerId: String) // это нужно запустить в самом начале работы программы
    {
        Log.d("LocationListener", "start listening")
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000, 10f,
            listener
        )
        viewModelScope.launch {
            startWebSocket(trackerId)
        }
    }

    fun stopSearch(context: Context)
    {
        Log.d("LocationListener", "stop listening")

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(listener)

        viewModelScope.launch {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "driver turn off transponder "))
        }
    }


    private var listener = LocationListener {
        val position = GeoPosition(latitude = it.latitude, longitude = it.longitude)
        viewModelScope.launch {
            busLocation.emit(position)
        }
    }
    private suspend fun startWebSocket(trackerId: String)
    {
        providerKtorCLient().webSocket(
            method = HttpMethod.Get,
            host = HOST,
            path = EndPoint.webSocket_driver + trackerId
        )
        {
            webSocketSession = this@webSocket

            busLocation.collect {
                Log.d("newLocation(driver)", "${it.latitude.toString()} ${it.longitude.toString()}")
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

    suspend fun getFlight(): List<FlightsNameResponse>? =
        try {
            clientRoutes.get(urlFlightNames)
        }
        catch (error: Exception) {
            Log.d("ErrorServer", error.message.toString())
            null
        }

    private fun providerKtorCLient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }
}