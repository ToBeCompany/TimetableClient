package com.dru128.timetable.admin.edituser

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dru128.timetable.EndPoint
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.User
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class EditUsersViewModel(application : Application): AndroidViewModel(application)
{
    var userList = MutableStateFlow(arrayOf<User>())

    suspend fun getUsers(): Array<User> =
        try {
            Log.d("Server", "SUCCESS")
            val response: Array<User> = Storage.client.get(EndPoint.allUsers)
            userList.value = response
            response
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            arrayOf()
        }



    suspend fun createUser(user: User): String? =
        try {
            Log.d("Server", "SUCCESS")
            Storage.client.post(EndPoint.createUser + user) {
                this.body = Json.encodeToString(user)
            }
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            null
        }

    suspend fun deleteUser(id: String): Boolean =
        try {
            Log.d("Server", "SUCCESS")
            val response: Boolean = Storage.client.get(EndPoint.deleteUser + id)
            if (response) // если удаление прош
            {
                userList.value
                    .filter { it.id != id }
                    .let { userList.value = it.toTypedArray() }
            }

            response
        }
        catch (error: Exception) {
            Log.d("Server", "ERROR: ${error.message}")
            false
        }
            //                val position = UsersRepository.userList.value.indexOf(UsersRepository.userList.value[position])
    //
    //                UsersRepository.userList.value.removeAt(position)
    //                notifyItemRemoved(position)
    //                notifyItemRangeChanged(position,  UsersRepository.userList.value.size)
}