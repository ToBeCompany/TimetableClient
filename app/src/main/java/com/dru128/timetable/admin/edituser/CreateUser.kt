package com.dru128.timetable.admin.edituser

import com.dru128.timetable.data.metadata.User

interface CreateUser
{
    fun createUser(user: User)
}