package com.dru128.timetable.tools

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


class Keyboard
{
    fun show(context: Context, view: View)
    {
        view.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
            ?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    fun hide(context: Context, view: View)
    {
        view.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
            ?.hideSoftInputFromWindow(view.windowToken, 0)

    }
}