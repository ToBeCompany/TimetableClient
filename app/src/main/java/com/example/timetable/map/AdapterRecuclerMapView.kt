package com.example.timetable.map

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.data.BusData
import com.example.timetable.data.BusStop

class AdapterRecuclerMapView(var data: BusData, public var cur: Int) : RecyclerView.Adapter<AdapterRecuclerMapView.ViewHolder>()
{
    var dataset: MutableList<BusStop>? = data.busStops
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(dataset!![position], position)
    }

    override fun getItemCount(): Int = dataset!!.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context
        var name_text = itemView.findViewById<TextView>(R.id.textViewOst)
        var time_text = itemView.findViewById<TextView>(R.id.textViewTime)

        fun onBind(busStop: BusStop, position: Int)
        {
            if (cur == position)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    itemView.setBackgroundColor(context.getColor(R.color.blue))
                }
            }

            name_text.text = busStop.name
                time_text.text = busStop.time
            }
    }

}