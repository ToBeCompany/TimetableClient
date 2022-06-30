package com.dru128.timetable.admin.edituser

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Repository
import com.dru128.timetable.data.metadata.User
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class EditUsersViewModel(application : Application): AndroidViewModel(application)
{
    suspend fun getUsers(): Boolean
    {
        try {

//            for (i in 0..10) UsersStorage.userList.value += User(TypeUser.WORKER, null, (0..91111).random().toString())
            val response: Array<User> = Repository.client.get(EndPoint.allUsers) ?: return false
            response.forEach { Log.d("data", "role = ${it.userType}, id = ${it.id}") }

            UsersStorage.userList = MutableStateFlow(response)
            Log.d("Server", "SUCCESS")
            return true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            return false
        }
    }

    suspend fun createUser(user: User): Boolean =
        try {
            val response: HttpResponse = Repository.client.post(EndPoint.createUser) {
                this.body = Json.encodeToString(user)
            }

            UsersStorage.userList.value += user
            Log.d("Server", "SUCCESS")
            true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            false
        }

    suspend fun deleteUser(id: String): Boolean =
        try {
            Log.d("Server", "SUCCESS")
            val response: Boolean? = Repository.client.get(EndPoint.deleteUser + id)

            UsersStorage.userList.value
                .filter { it.id != id }
                .let { UsersStorage.userList.value = it.toTypedArray() }
            true
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            false
        }

    //                val position = UsersRepository.userList.value.indexOf(UsersRepository.userList.value[position])
    //                UsersRepository.userList.value.removeAt(position)
    //                notifyItemRemoved(position)
    //                notifyItemRangeChanged(position,  UsersRepository.userList.value.size)
}