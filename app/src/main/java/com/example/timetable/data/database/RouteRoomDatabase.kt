package com.example.timetable.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.timetable.data.RouteComplex


abstract class RouteRoomDatabase: RoomDatabase()
{
    abstract fun routeDao(): RouteDao

}