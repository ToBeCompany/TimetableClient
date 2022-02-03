package com.dru128.timetable.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.data.room.entity.RouteComplex


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