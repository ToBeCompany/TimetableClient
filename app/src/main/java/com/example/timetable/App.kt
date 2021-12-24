package com.example.timetable

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate


class App: Application() // переопределяем класс приложения
{
    override fun onCreate()
    {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // запрещаю включать тёмную тему


        globalContext = applicationContext
    }

    companion object {
        lateinit var globalContext: Context
    }
}