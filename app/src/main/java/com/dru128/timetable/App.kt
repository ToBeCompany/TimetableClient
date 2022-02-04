package com.dru128.timetable

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.dru128.timetable.data.room.RouteRoomDatabase
import com.dru128.timetable.driver.service.NotificationDriver


class App: Application() // переопределяем класс приложения
{
    val database: RouteRoomDatabase by lazy { RouteRoomDatabase.getDatabase(this) }

    override fun onCreate()
    {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // запрещаю включать тёмную тему
        NotificationDriver.createNotificationChannel(this)

        globalContext = applicationContext

//        Storage.routes = RoomDataManager(this).loadRoutes()

    }



    companion object {
        lateinit var globalContext: Context
    }
}