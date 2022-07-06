package com.dru128.timetable.admin.edituser

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.data.metadata.User
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement


class EditUsersViewModel(application : Application): AndroidViewModel(application)
{
    suspend fun getUsers(): Boolean
    {
        try {

//            for (i in 0..10) UsersStorage.userList.value += User(TypeUser.WORKER, null, (0..91111).random().toString())
            val response: HttpResponse = Repository.client.get(EndPoint.allUsers)
            Log.d("Server", "Status code: ${response.status.value}")
            if (response.status.value == 200)
            {
                val users = response.body<Array<User>>()

                users.forEach { Log.d("data", "role = ${it.userType}, id = ${it.id}") }
                UsersStorage.userList = MutableStateFlow(users)
                Log.d("Server", "SUCCESS")
                return true
            }
            else
                return false
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }

    suspend fun createUser(user: User): Boolean
    {
        try {
            val response: HttpResponse = Repository.client.post(EndPoint.createUser) {
                this.setBody(Json.encodeToJsonElement(user))
            }
            Log.d("Server", "Status code: ${response.status.value}")

            if (response.status.value == 200)
            {
                UsersStorage.userList.value += user
                Log.d("Server", "SUCCESS")
                return true
            } else
                return false

        } catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }
    suspend fun deleteUser(id: String): Boolean
    {
        try {
            val response: HttpResponse = Repository.client.delete(EndPoint.deleteUser) {
                this.setBody(id)
            }
            Log.d("Server", "Status code: ${response.status.value}")

            if (response.status.value == 200)
            {
                UsersStorage.userList.value
                    .filter { it.id != id }
                    .let { UsersStorage.userList.value = it.toTypedArray() }
                Log.d("Server", "SUCCESS")
                return true
            } else
                return false
        } catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }
    //                val position = UsersRepository.userList.value.indexOf(UsersRepository.userList.value[position])
    //                UsersRepository.userList.value.removeAt(position)
    //                notifyItemRemoved(position)
    //                notifyItemRangeChanged(position,  UsersRepository.userList.value.size)
}