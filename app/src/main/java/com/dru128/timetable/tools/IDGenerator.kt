package com.dru128.timetable.tools

import org.apache.commons.codec.binary.Base64
import java.nio.ByteBuffer
import java.util.*

object IDGenerator
{
    fun generateID(): String // генератор UUID (уникального в мире)
            = uuidToBase64(UUID.randomUUID())

    private fun uuidToBase64(uuid: UUID): String
    {
        val base64 = Base64()
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return base64.encodeToString(bb.array())
            .replace("=", "")
            .replace("/", "")
            .replace("""\""", "")
            .replace("*", "")
    }
}