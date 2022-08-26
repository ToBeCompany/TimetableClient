package com.dru128.timetable.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.data.metadata.GeoPosition
import dru128.timetable.R
import dru128.timetable.databinding.BusStopItemWorkerBinding


class BusStopsRecycleAdapter(
    var current: String,
    var dataSet: Array<BusStopWithTime>,
    var itemClick: (id: GeoPosition) -> Unit
)
    : RecyclerView.Adapter<BusStopsRecycleAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = BusStopItemWorkerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(var binding: BusStopItemWorkerBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = itemView.context

        fun onBind(index: Int)
        {
            if (dataSet[index].busStop.id == current)
                binding.parent.setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.light_blue, null))

            binding.busStopName.text = dataSet[index].busStop.name
            binding.busStopTime.text = dataSet[index].time
            itemView.setOnClickListener {
                itemClick(dataSet[index].busStop.position)
            }
        }
    }
}