package com.dru128.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.data.metadata.GeoPosition
import dru128.timetable.R
import kotlin.reflect.KFunction1


class BusStopsRecycleAdapter(
    var current: String,
    var dataSet: List<BusStopWithTime>,
    var itemClick: KFunction1<GeoPosition, Unit>
)
    : RecyclerView.Adapter<BusStopsRecycleAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.busstop_item, parent, false)
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context
        var name_text = itemView.findViewById<TextView>(R.id.busStopName_busstop_item)
        var time_text = itemView.findViewById<TextView>(R.id.time_busstop_item)

        fun onBind(index: Int)
        {
            if (dataSet[index].busStop.id == current)
                itemView.setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.light_blue, null))
            name_text.text = dataSet[index].busStop.name
            time_text.text = dataSet[index].time
            itemView.setOnClickListener {
                itemClick(dataSet[index].busStop.position)
            }
        }
    }
}