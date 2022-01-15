package com.example.timetable.data.database

import androidx.room.*
import com.example.timetable.data.Route
import kotlinx.coroutines.flow.Flow


@Dao
interface RouteDao
{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Route)

    @Update
    suspend fun update(item: Route)

    @Delete
    suspend fun delete(item: Route)

    @Query("SELECT * from route WHERE id = :id")
    fun getRoute(id: String): Flow<Route>

    @Query("SELECT * from route ORDER BY name ASC")
    fun getRoutes(): Flow<List<Route>>
}