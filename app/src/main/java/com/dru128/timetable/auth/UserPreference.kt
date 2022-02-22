package com.dru128.timetable.auth

import android.content.Context
import androidx.preference.PreferenceManager
import com.dru128.timetable.data.metadata.User
import com.dru128.timetable.data.metadata.type_user_creater


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

    fun checkAuth(): Boolean = preference.getBoolean(TAG_AUTH, false)

    private fun getDataUser(): User
    {
        preference.apply {
            return User(
                userType = type_user_creater(getString(User.TAG_USERTYPE, "")),
                id = getString(User.TAG_ID, "").toString(),
                name = getString(User.TAG_NAME, "").toString()
            )
        }
    }

    fun loginOut()
    {
        currentUser = null
        preference
            .edit()
            .clear()
            .putBoolean(TAG_AUTH, false)
            .apply()
    }
}