package com.dru128.timetable.worker.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.data.metadata.GeoPosition
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.net.SocketException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class MapWorkerViewModel: ViewModel()
{
    var webSocketSession: DefaultClientWebSocketSession? = null
    var isTracking = false

    private val _geoPosition : MutableStateFlow<GeoPosition?> = MutableStateFlow(null)
    val geoPosition : StateFlow<GeoPosition?> = _geoPosition

    fun startWebSocket(routeId: String){
        isTracking = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Repository.websocketClient().webSocket(
                    method = HttpMethod.Get,
                    host = EndPoint.host,
                    path = (EndPoint.webSocket_passenger + routeId)
                )
                {
                    webSocketSession = this@webSocket
                    Log.d("WEB_SOCKET", "start client map routeId = $routeId")

                    for (frame in incoming) {
                        Log.d("WEB_SOCKET", "update position111")
                        if (frame is Frame.Text) {
                            Log.d("WEB_SOCKET", frame.readText())
                            val position = Json.decodeFromString<GeoPosition?>(frame.readText())
                            position?.let {
                                _geoPosition.value = it
                            }
                            Log.d("WEB_SOCKET", "update position: $position")
                        }
                    }
                }
            } catch (e : SocketException){
                isTracking = false
            }
        }
    }

    fun stopWebSocket() {
        isTracking = false
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("WEB_SOCKET", "close web socket")
            webSocketSession?.close(
                CloseReason(
                    CloseReason.Codes.NORMAL,
                    "map fragment was closed"
                )
            )
        }
    }
}