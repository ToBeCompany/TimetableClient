package com.example.timetable.system

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.timetable.R


class ProgressManager(var container: ConstraintLayout)
{
    var progressBar: ProgressBar? = null

    fun start()
    {

        progressBar = ProgressBar(ContextThemeWrapper(container.context , R.style.Theme_TimeTable_ProgressBar))

        progressBar!!.id = View.generateViewId()
        progressBar!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(progressBar)
        val constraintSet = ConstraintSet().apply {
            clone(container)
            connect(progressBar!!.id, ConstraintSet.BOTTOM, container.id, ConstraintSet.BOTTOM)
            connect(progressBar!!.id, ConstraintSet.END, container.id, ConstraintSet.END)
            connect(progressBar!!.id, ConstraintSet.START, container.id, ConstraintSet.START)
            connect(progressBar!!.id, ConstraintSet.TOP, container.id, ConstraintSet.TOP)
            applyTo(container)
        }
    }

    fun finish()
    {
//        container.isEnabled = true
        container.removeView(progressBar)
    }
}