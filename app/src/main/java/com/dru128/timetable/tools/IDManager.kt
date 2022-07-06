package com.dru128.timetable.tools

import java.nio.ByteBuffer
import java.util.*
import org.apache.commons.codec.binary.Base64


object IDManager
{
    const val dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"


    fun generateID(): String // генератор UUID (уникального в мире)
    {
        while (true)
        {
            val uuid = UUID.randomUUID()
            val base64 = Base64()
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            val stringId = base64.encodeToString(bb.array())
                .replace("=", "")
                .replace("/", "")
                .replace("""\""", "")
            if (validateId(stringId)) {
                return stringId
            }
        }
    }

    fun validateId(id: String): Boolean
        = id.all { dictionary.contains(it) }
}