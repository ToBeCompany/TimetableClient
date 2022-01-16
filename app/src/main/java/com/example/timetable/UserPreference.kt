package com.example.timetable

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.timetable.data.metadata.User
import com.example.timetable.data.metadata.type_user_creater

class UserPreference(var context: Context)
{
    private val TAG_AUTH: String = "TAG_AUTH"
    private var preference = PreferenceManager.getDefaultSharedPreferences(context)

    var currentUser: User? = null
    get()
    {
        if (field == null)
        {
            if (checkAuth())
            {
                field = getDataUser()
            }
        }
        return field
    }

    fun authUserOnDevice(user: User)
    {
        preference.edit().apply {
            putBoolean(TAG_AUTH, true)
            putString(User.TAG_NAME, user.name)
            putString(User.TAG_ID, user.id)
            putString(User.TAG_USERTYPE, user.userType.toString())
            apply()
        }
    }

    private fun checkAuth() = preference.getBoolean(TAG_AUTH, false)

    private fun getDataUser(): User
    {
        preference.apply {
            return User(
                userType = type_user_creater(getString(User.TAG_USERTYPE, "")),
                id = getString(User.TAG_ID, ""),
                name = getString(User.TAG_NAME, "")
            )
        }
    }

    fun loginOut()
    {
        currentUser = null
        preference
            .edit()
            .putBoolean(TAG_AUTH, false)
            .apply()
    }
}