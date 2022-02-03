package com.dru128.timetable.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.data.room.entity.RouteComplex


@Dao
interface RouteDao
{
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(item: RouteComplex)
//
//    @Update
//    suspend fun update(item: RouteComplex)
//
//    @Delete
//    suspend fun delete(item: RouteComplex)

//    @Query("SELECT * from route WHERE id = :id")
//    fun getRoute(id: String): Flow<RouteComplex>
/* ------------------------------------------------------------------------------------------------ */
//    @Query("SELECT * from rou")
//    fun getRoutes(): List<Route>
//
//    @Insert
//    fun addRoute(route: RouteComplex)



//    @Query(
//        "SELECT * FROM Route " +
//                "INNER JOIN routepoint ON routepoint.route_id = route "
////                "INNER JOIN user ON user.id = loan.user_id "
////                "WHERE user.name LIKE :userName"
//    )
//    fun findBooksBorrowedByNameSync(route: Route): List<Route>

}