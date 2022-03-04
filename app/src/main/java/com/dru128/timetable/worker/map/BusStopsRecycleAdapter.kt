package com.dru128.timetable.worker.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.BusStopWithTime
import dru128.timetable.R
import dru128.timetable.databinding.BusstopItemBinding


class BusStopsRecycleAdapter(
    var current: Int,
    var dataSet: List<BusStopWithTime>,
    var itemClick: (Int) -> Unit
): RecyclerView.Adapter<BusStopsRecycleAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = BusstopItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(val binding: BusstopItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context
        fun onBind(index: Int)
        {
            if (dataSet[current] == dataSet[index])
            {
                binding.parentLayout.setBackgroundColor( ResourcesCompat.getColor(context.resources, R.color.light_blue, null) )
            }
            binding.nameText.text = dataSet[index].busStop?.name ?: ""
            binding.timeText.text = dataSet[index].time ?: ""

            binding.parentLayout.setOnClickListener {
                itemClick(index)
            }
        }
    }
}