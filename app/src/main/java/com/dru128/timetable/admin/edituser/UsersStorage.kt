package com.dru128.timetable.admin.edituser

import com.dru128.timetable.data.metadata.User
import kotlinx.coroutines.flow.MutableStateFlow

object UsersStorage
{
    var userList: MutableStateFlow<Array<User>> = MutableStateFlow(arrayOf<User>())
}