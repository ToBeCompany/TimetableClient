package com.example.timetable

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.timetable.data.database.RouteRoomDatabase
import com.example.timetable.driver.service.NotificationDriver


class App: Application() // переопределяем класс приложения
{

    override fun onCreate()
    {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // запрещаю включать тёмную тему
        NotificationDriver.createNotificationChannel(this)

        globalContext = applicationContext
    }

    companion object {
        lateinit var globalContext: Context
    }
}