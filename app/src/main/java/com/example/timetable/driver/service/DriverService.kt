package com.example.timetable.driver.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.timetable.EndPoint
import com.example.timetable.MainActivity
import com.example.timetable.R
import com.example.timetable.data.GeoPoint
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.Security
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.annotation.WorkerThread
import com.example.timetable.App
import com.example.timetable.data.metadata.response.FlightsNameResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.encodeToJsonElement


class DriverService() : Service()
{
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private var route: FlightsNameResponse? = null

    private val busLocation = MutableSharedFlow<GeoPoint>()
    private val HOST = EndPoint.host


    private var listener = LocationListener {
        val position = GeoPoint(latitude = it.latitude, longitude = it.longitude)
        MainScope().launch {
            busLocation.emit(position)
        }
    }

    val networkCallback = object: ConnectivityManager.NetworkCallback()
    {
        // сеть доступна для использования
        override fun onAvailable(network: Network) {
            startSearch(route?.id.toString())
            Log.d("network", "is on")
            super.onAvailable(network)
        }

        // соединение прервано
        override fun onLost(network: Network) {
            stopSearch()
            Log.d("network", "is off")
            super.onLost(network)
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
        when (action)
        {
            getString(R.string.on_service) ->
            {
                val _route: FlightsNameResponse = Json.decodeFromString(intent?.getStringExtra(getString(R.string.route)).toString())
                Log.d("startServiceAndTracker", _route.toString())
                startSearch(_route.id!!)
                route = _route
            }
            getString(R.string.off_service) ->
            {
                stopSearch()
                stopSelf()
            }
            getString(R.string.new_tracker) ->
            {
                stopSearch()
                val _route: FlightsNameResponse = Json.decodeFromString(intent?.getStringExtra(getString(R.string.route)).toString())
                startSearch(_route.id!!)
                route = _route
            }
            getString(R.string.which_tracker_id) ->
            {
                var _intent = Intent(baseContext, MainActivity::class.java).apply {
                    putExtra(getString(R.string.tracker_id), Json.encodeToString(route))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                baseContext.startActivity(_intent)
            }
        }


        val connectivityManager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java)
        connectivityManager!!.registerDefaultNetworkCallback(networkCallback)

        return super.onStartCommand(intent, flags, startId)
    }

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
                Log.d("newLocationTracker id= $trackerId", "${it.latitude.toString()} ${it.longitude.toString()}")
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