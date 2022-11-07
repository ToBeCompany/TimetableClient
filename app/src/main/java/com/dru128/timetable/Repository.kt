package com.dru128.timetable

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import java.security.Security
import kotlinx.serialization.json.Json


object Repository
{
    var client = HttpClient(Android) {
        install(Logging)
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true

                }
            )
        }
        defaultRequest {
            url.takeFrom(
                    URLBuilder().takeFrom(EndPoint.protocol + EndPoint.host).apply {
                        contentType(ContentType.Application.Json)
                        encodedPath += url.encodedPath
                }
            )
        }
    }

    fun websocketClient(): HttpClient
    {
        System.setProperty("io.ktor.util.random.secure.random.provider","DRBG")
        Security.setProperty("securerandom.drgb.config", "HMAC_DRBG,SHA-512,256,pr_and_reseed")

        return HttpClient(OkHttp) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    }
}