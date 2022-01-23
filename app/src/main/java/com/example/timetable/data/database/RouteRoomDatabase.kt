package com.example.timetable.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.timetable.data.RouteComplex


@Database(entities = [RouteComplex::class], version = 1, exportSchema = false)
abstract class RouteRoomDatabase: RoomDatabase()
{
    abstract fun routeDao(): RouteDao


    companion object
    {
        @Volatile
        private var INSTANCE: RouteRoomDatabase? = null


        fun getDatabase(context: Context): RouteRoomDatabase
        {
            return INSTANCE ?: synchronized(this)
            {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RouteRoomDatabase::class.java,
                    "route_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }

}