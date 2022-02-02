package com.dru128.timetable.driver

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter


class RouteArrayAdapter(
    context: Context,
    textViewResourceId: Int,
    var dataSet: Array<String>,
    private val hidingItem: String
) :
    ArrayAdapter<String?>(context, textViewResourceId, dataSet)
{

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        return if (dataSet[position] == hidingItem)
            View(context).apply { visibility = View.GONE }
        else
            super.getDropDownView(position, null, parent)
    }
}