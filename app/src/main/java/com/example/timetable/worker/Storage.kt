package com.example.timetable.worker

import com.example.timetable.data.Cache
import com.example.timetable.data.JsonCache
import com.example.timetable.data.PreferenceCache
import com.example.timetable.data.metadata.response.FlightsNameResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer


object Storage
{

    private val cache : Cache = JsonCache()
    fun loadCache(){

    }
    val BASE_URL = "https://fierce-woodland-54822.herokuapp.com"

    val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
//                    acceptContentTypes += ContentType("text", "plain")
        }
    }

    suspend fun getAllRouts() : {
        if (cache == null){
            cache = client.get<>()
        }
        return cache
    }
//    var routes: List<Route> = listOf()

    var flightsNames: List<FlightsNameResponse> = listOf()

}