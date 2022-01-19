package com.example.timetable.driver.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.timetable.EndPoint
import com.example.timetable.R
import com.example.timetable.data.GeoPoint
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.Security


class DriverService : Service()
{
    private val busLocation = MutableSharedFlow<GeoPoint>()
    private val HOST = EndPoint.host

    private var listener = LocationListener {
        val position = GeoPoint(latitude = it.latitude, longitude = it.longitude)
        MainScope().launch {
            busLocation.emit(position)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        val notification = NotificationDriver.track(this)
        NotificationManagerCompat
            .from(this)
            .notify(4,notification)
        startForeground(4, notification)

        val action: String = intent?.getStringExtra(getString(R.string.action)).toString()
        val trackerId: String = intent?.getStringExtra(getString(R.string.tracker_id)).toString()
        when (action)
        {
            getString(R.string.on_service) ->
            {
                startSearch(trackerId)
            }
            getString(R.string.off_service) ->
            {
                stopSearch()
                stopSelf()
            }
            getString(R.string.new_tracker) ->
            {
                stopSearch()
                startSearch(trackerId)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    var webSocketSession: DefaultClientWebSocketSession? = null

    @SuppressLint("MissingPermission")
    private fun startSearch(trackerId: String) // это нужно запустить в самом начале работы программы
    {
        Log.d("LocationListener", "start listening")
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000, 10f,
            listener
        )
        MainScope().launch {
            startWebSocket(trackerId)
        }
    }

    private fun stopSearch()
    {
        Log.d("LocationListener", "stop listening")

        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(listener)

        MainScope().launch {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "driver turn off transponder "))
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
                Log.d("newLocation(driver) $trackerId", "${it.latitude.toString()} ${it.longitude.toString()}")
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


    private fun providerKtorCLient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}