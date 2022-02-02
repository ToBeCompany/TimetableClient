package com.dru128.timetable.system

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import dru128.timetable.R


class ProgressManager(var container: ConstraintLayout, var activity: FragmentActivity)
{
    lateinit var progressBar: ProgressBar
    lateinit var shadowView: View

    fun start()
    {
        val context = container.context

        activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        shadowView = View(context)
        progressBar = ProgressBar(ContextThemeWrapper(context , R.style.Theme_TimeTable_ProgressBar))

        shadowView.setBackgroundColor(ResourcesCompat.getColor(container.context.resources, R.color.progress_shadow, null))

        shadowView.id = View.generateViewId()
        progressBar.id = View.generateViewId()

        shadowView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        progressBar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        container.addView(shadowView)
        container.addView(progressBar)
        ConstraintSet().apply {
            clone(container)
            connect(progressBar.id, ConstraintSet.BOTTOM, container.id, ConstraintSet.BOTTOM)
            connect(progressBar.id, ConstraintSet.END, container.id, ConstraintSet.END)
            connect(progressBar.id, ConstraintSet.START, container.id, ConstraintSet.START)
            connect(progressBar.id, ConstraintSet.TOP, container.id, ConstraintSet.TOP)
            connect(shadowView.id, ConstraintSet.BOTTOM, container.id, ConstraintSet.BOTTOM)
            connect(shadowView.id, ConstraintSet.END, container.id, ConstraintSet.END)
            connect(shadowView.id, ConstraintSet.START, container.id, ConstraintSet.START)
            connect(shadowView.id, ConstraintSet.TOP, container.id, ConstraintSet.TOP)
            applyTo(container)
        }
    }

    fun finish()
    {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        container.removeView(progressBar)
        container.removeView(shadowView)
    }
}