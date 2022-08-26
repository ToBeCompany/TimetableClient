package com.dru128.timetable.admin.map.create_route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.BusStopWithTime
import dru128.timetable.databinding.BusStopAdminItemBinding


class BusStopAdminRecyclerAdapter (
    var deleteBusStop: (id: String) -> Unit,
    var busStopChanged: (busStop: BusStopWithTime) -> Unit,
    var dataSet: Array<BusStopWithTime>
) : RecyclerView.Adapter<BusStopAdminRecyclerAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = BusStopAdminItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(var binding: BusStopAdminItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context

        fun onBind(position: Int)
        {
            binding.apply {
                nameText.setText(dataSet[position].busStop.name)
                timeText.setText(dataSet[position].time)

                deleteBusStopButton.setOnClickListener {
                    deleteBusStop(dataSet[position].busStop.id)
                }

                nameText.doOnTextChanged { text, start, count, after ->
                    val busStop = dataSet[position]
                    busStop.busStop.name = text.toString()
                    busStopChanged(busStop)
                }
                timeText.doOnTextChanged { text, start, count, after ->
                    val busStop = dataSet[position]
                    busStop.time = text.toString()
                    busStopChanged(busStop)
                }

            }
        }
    }
}