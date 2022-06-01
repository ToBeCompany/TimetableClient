package com.dru128.timetable.tools

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.ClipboardManager


object Copy
{
    fun copyText(context: Context, copiedText: String)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = copiedText
        } else
        {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("COPY", copiedText)
            clipboard.setPrimaryClip(clip)
        }
    }
}