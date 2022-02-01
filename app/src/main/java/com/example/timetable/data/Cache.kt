package com.example.timetable.data

import android.content.Context

interface Cache {
    suspend fun saveRouts()
    suspend fun loadRouts()
}

class PreferenceCache(val context: Context) : Cache{
    override suspend fun saveRouts() {
//        context.getSharedPreferences("asd")

    }

    override suspend fun loadRouts() {
    }
}

class JsonCache() : Cache{
    override suspend fun saveRouts() {

    }

    override suspend fun loadRouts() {
        TODO("Not yet implemented")
    }
}