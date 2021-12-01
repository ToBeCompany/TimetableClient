package com.example.timetable.data.n

enum class TypeUser { DRIVER, COMMON, ADMIN}

fun type_user_creater(s : String?) = when(s) // конвертирует из string в enum class
{
    "DRIVER" -> TypeUser.DRIVER
    "COMMON" -> TypeUser.COMMON
    "ADMIN" -> TypeUser.ADMIN
    else -> TypeUser.COMMON
}