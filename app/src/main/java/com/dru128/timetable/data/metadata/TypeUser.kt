package com.dru128.timetable.data.metadata

import kotlin.String
import kotlinx.serialization.Serializable


@Serializable
enum class TypeUser { DRIVER, WORKER, ADMIN }

fun type_user_creater(s : String?): TypeUser = when(s) // конвертирует из string в enum class
{
    "DRIVER" -> TypeUser.DRIVER
    "COMMON" -> TypeUser.WORKER
    "ADMIN" -> TypeUser.ADMIN
    else -> TypeUser.WORKER
}