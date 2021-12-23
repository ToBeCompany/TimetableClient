package com.example.timetable.data

import kotlinx.serialization.Serializable


@Serializable
class User(
    var userType: TypeUser?,
    var name: String?,
    var id: String?
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