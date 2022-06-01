package com.dru128.timetable.worker.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.data.metadata.GeoPosition
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class MapWorkerViewModel(application : Application): AndroidViewModel(application) {
    var webSocketSession: DefaultClientWebSocketSession? = null

    fun startWebSocket(routeId: kotlin.String) = flow<GeoPosition> {
        Repository.websocketClient().webSocket(
            method = HttpMethod.Get,
            host = EndPoint.host,
            path = (EndPoint.webSocket_passenger + routeId)
        )
        {
            webSocketSession = this@webSocket
            Log.d("WEB_SOCKET", "start client map routeId = $routeId")
            for (frame in incoming)
            {
                if (frame is Frame.Text)
                {
                    val position = Json.decodeFromString<GeoPosition>(frame.readText())
                    emit(position)
                    Log.d("WEB_SOCKET", "update position: " + position.latitude.toString())
                }
            }
        }

    }.flowOn(Dispatchers.IO)

    fun stopWebSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("WEB_SOCKET", "close web socket")
            webSocketSession?.close(
                CloseReason(
                    CloseReason.Codes.NORMAL,
                    "user close map fragment"
                )
            )
        }
    }
}