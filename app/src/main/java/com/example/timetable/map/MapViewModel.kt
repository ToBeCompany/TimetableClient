package com.example.timetable.map


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.data.BusData
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.security.Security

class MapViewModel(application : Application): AndroidViewModel(application)
{
    private var id = "1"
    private val HOST = "fierce-woodland-54822.herokuapp.com"
    private val PATH = "/passenger/$id"

    var busData: BusData? = null
    val busLocation: Flow<LatLng> = flow {
        for (i in 0..200) {
            emit(LatLng(
                i.toDouble()/5,
                50.0
            ))
            kotlinx.coroutines.delay(300)
        }
    }

//    fun updateLocation(newData: LatLng) { busLocation.value = Resource.Success(newData) }
        //https://habr.com/ru/post/432310/
    private fun providerKtorCLient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }
    fun startWebSocket() = flow<String> {
            val client = providerKtorCLient().webSocket(
                method = HttpMethod.Get,
                host = HOST,
                path = PATH
            )
            {

                for(frame in incoming){
                    when (frame){
                        is Frame.Text -> {

//                            Log.d("getDAta", frame.readText())
//                            val data : User= Json.decodeFromString<User>(frame.readText())
                            emit(frame.readText())
                                //https://question-it.com/questions/2640377/kak-serializovat-web-socket-frametext-v-ktor-s-pomoschju-kotlinxserialization
                        }
                    }
                }
//                    for (response in incoming)
//                    {
//                        when(response){
//                            is Frame.Binary -> {
////                                (response.data.decode() as LatLng)
//                            }
//                        }
//                        var data = response.readBytes()
//                        //emit(response.receive readBytes() as LatLng) // !_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_!_
//
//                    }
            }

    }.flowOn(Dispatchers.IO)

}