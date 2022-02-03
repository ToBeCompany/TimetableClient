package com.dru128.timetable.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dru128.timetable.R
import com.dru128.timetable.data.metadata.BusStopWithTime


class BusStopsRecycleAdapter(var current: Int, var dataSet: List<BusStopWithTime>)
    : RecyclerView.Adapter<BusStopsRecycleAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.busstop_item, parent, false)
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context
        var name_text = itemView.findViewById<TextView>(R.id.busStopName_busstop_item)
        var time_text = itemView.findViewById<TextView>(R.id.time_busstop_item)

        fun onBind(busStopWithTime: BusStopWithTime)
        {
            if (dataSet[current] == busStopWithTime)
            {
                itemView.setBackgroundColor( context.resources.getColor(R.color.light_blue) )
//                itemView.findViewById<TextView>(R.id.textViewOst).setTextColor(context.resources.getColor(R.color.black))
//                itemView.findViewById<TextView>(R.id.textViewTime).setTextColor(context.resources.getColor(R.color.grey_new))
//                itemView.findViewById<ImageView>(R.id.imageView4).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_lens_24))
            }


            name_text.text = busStopWithTime.busStop?.name ?: ""
            time_text.text = busStopWithTime.time ?: ""

        }
    }

}