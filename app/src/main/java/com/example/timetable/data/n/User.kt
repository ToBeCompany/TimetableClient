package com.example.timetable.data.n

class User(
    var userType: TypeUser?,
    var name: String?,
    var id: String?
)
{
    companion object
    {
        const val TAG_ID: String = "TAG_ID"
        const val TAG_NAME: String = "TAG_NAME"
        const val TAG_USERTYPE: String = "TAG_USERTYPE"
    }

}