package com.example.timetable.data.database

import androidx.room.*
import com.example.timetable.data.Route
import com.example.timetable.data.RouteComplex
import kotlinx.coroutines.flow.Flow


@Dao
interface RouteDao
{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: RouteComplex)

    @Update
    suspend fun update(item: RouteComplex)

    @Delete
    suspend fun delete(item: RouteComplex)

//    @Query("SELECT * from route WHERE id = :id")
//    fun getRoute(id: String): Flow<RouteComplex>
//
//    @Query("SELECT * from route ORDER BY name ASC")
//    fun getRoutes(): Flow<List<RouteComplex>>
}