package com.example.timetable.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Delete
import com.example.timetable.data.RouteComplex


@Dao
interface RouteDao
{

//    @Query("SELECT * from route WHERE id = :id")
//    fun getRoute(id: String): Flow<RouteComplex>
//
//    @Query("SELECT * from route ORDER BY name ASC")
//    fun getRoutes(): Flow<List<RouteComplex>>
}