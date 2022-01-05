package com.example.timetable.map

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.data.BusStop


class BusStopsRecycleAdapter(var current: Int, var dataSet: List<BusStop>)
    : RecyclerView.Adapter<BusStopsRecycleAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
        return ViewHolder(View)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context
        var name_text = itemView.findViewById<TextView>(R.id.textViewOst)
        var time_text = itemView.findViewById<TextView>(R.id.textViewTime)


        fun onBind(busStop: BusStop)
        {
            if (dataSet[current] == busStop)
            {
                itemView.findViewById<TextView>(R.id.textViewOst).setTextColor(context.resources.getColor(R.color.black))
                itemView.findViewById<TextView>(R.id.textViewTime).setTextColor(context.resources.getColor(R.color.grey_new))
                itemView.findViewById<ImageView>(R.id.imageView4).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_lens_24))
            }


            name_text.text = busStop.name
//                time_text.text = busStop.time
            }
    }

}