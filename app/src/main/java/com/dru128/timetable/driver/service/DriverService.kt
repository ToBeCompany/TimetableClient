package com.dru128.timetable.driver.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import dru128.timetable.R
import com.dru128.timetable.data.metadata.GeoPosition
import com.dru128.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class DriverService() : Service()
{
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private var route: FlightsNameResponse? = null

    private val busLocation = MutableSharedFlow<GeoPosition>()

    private var listener = LocationListener {
        val position = GeoPosition(latitude = it.latitude, longitude = it.longitude)
        MainScope().launch(Dispatchers.IO) {
            busLocation.emit(position)
        }
    }
    private val wakeLock: PowerManager.WakeLock  by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${getString(R.string.app_name)}::WakelockTag")
        }
    }
    private val networkCallback = object: ConnectivityManager.NetworkCallback()
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

        wakeLock.acquire() // запрещаю переходить в спящий режим

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
                val _intent = Intent(getString(R.string.action))
                _intent.putExtra(getString(R.string.off_service), getString(R.string.off_service))
                LocalBroadcastManager
                    .getInstance(applicationContext)
                    .sendBroadcast(_intent)
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
                val _intent = Intent(getString(R.string.action))
                _intent.putExtra(getString(R.string.tracker_id), Json.encodeToString(route))
                LocalBroadcastManager
                    .getInstance(applicationContext)
                    .sendBroadcast(_intent)
            }
        }


        val connectivityManager = ContextCompat.getSystemService(applicationContext, ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager!!.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager!!.registerNetworkCallback(request, networkCallback)
        }
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
        MainScope().launch(Dispatchers.IO) {
            startWebSocket(trackerId)
        }
    }

    private fun stopSearch()
    {
        Log.d("LocationListener", "stop listening")

        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(listener)

        MainScope().launch(Dispatchers.IO) {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "driver turn off transponder "))
        }
    }


    private suspend fun startWebSocket(trackerId: String)
    {
        Storage.websocketClient().webSocket(
            method = HttpMethod.Get,
            host = EndPoint.host,
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        wakeLock.release() // разрешаю переходить в спящий режим
        super.onDestroy()
    }
}