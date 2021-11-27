package com.example.timetable

import android.app.Application
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class App: Application() // переопределяем класс приложения
{
    override fun onCreate()
    {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // запрещаю включать тёмную тему


    }
}