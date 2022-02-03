package com.dru128.timetable

import com.dru128.timetable.data.metadata.Route
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import java.security.Security


object Storage
{
    var routes: Array<Route> = arrayOf()



    var client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
        defaultRequest{
            url.takeFrom( URLBuilder ().takeFrom(EndPoint.protocol + EndPoint.host).apply {
                encodedPath += url.encodedPath
            })
        }
    }

    fun websocketClient(): HttpClient
    {
        System.setProperty("io.ktor.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config","HMAC_DRBG,SHA-512,256,pr_and_reseed")
        return HttpClient(CIO) {
            install(WebSockets)
        }
    }
}