package com.dru128.timetable.driver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dru128.timetable.MainActivity
import dru128.timetable.R


object NotificationDriver
{
    private val CHANNEL_ID: String = "TRACK_NOTIFICATION"

    fun track(context: Context) :Notification
    {
        val pendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        val closeServes: PendingIntent =
            Intent(context, DriverService::class.java).let { closeServesIntent ->
                closeServesIntent.putExtra(context.getString(R.string.action), context.getString(R.string.off_service))
                PendingIntent.getService(context, 0, closeServesIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_gps_fixed)
            .setContentTitle(context.getString(R.string.gps_tracker))
            .setContentText(context.getString(R.string.click_to_open_app))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.close, "Выключить трекер", closeServes)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun createNotificationChannel(context: Context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "CHANNEL_NAME"
            val descriptionText = "CHANNEL DESCRIPTION"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}