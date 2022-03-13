package com.dru128.timetable

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.dru128.timetable.driver.service.NotificationDriver
import leakcanary.AppWatcher
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module


class App: Application() // переопределяем класс приложения
{
    override fun onCreate()
    {
        super.onCreate()
        AppWatcher.objectWatcher.expectWeaklyReachable(this, "App detached canary")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // запрещаю включать тёмную тему
        NotificationDriver.createNotificationChannel(this)

        globalContext = applicationContext

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(
                listOf(applicationModules)
            )
        }

    }

    companion object {
        lateinit var globalContext: Context
    }
}