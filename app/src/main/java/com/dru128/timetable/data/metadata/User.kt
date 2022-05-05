package com.dru128.timetable.data.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class User( // пользователь
    @SerialName("User_type") var userType: TypeUser = TypeUser.WORKER,
    var name: String? = "",
    var id: String = ""
)





































{
    fun isNotEmpy() = !(userType == null ||
                        name == null ||
                        id == null)

    companion object
    {
        const val TAG_ID: String = "TAG_ID"
        const val TAG_NAME: String = "TAG_NAME"
        const val TAG_USERTYPE: String = "TAG_USERTYPE"
    }
}