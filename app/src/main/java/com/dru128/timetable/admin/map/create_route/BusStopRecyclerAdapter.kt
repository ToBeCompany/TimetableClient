package com.dru128.timetable.admin.map.create_route

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.BusStopWithTime
import dru128.timetable.R
import dru128.timetable.databinding.BusStopAdminItemBinding
import kotlinx.coroutines.channels.ticker


class BusStopAdminRecyclerAdapter (
    var deleteBusStop: (id: String) -> Unit,
    var changeBusStop: (bundle: Bundle) -> Unit,
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
            val id = dataSet[position].busStop.id
            fun changeBusStopData()
            {
                val bundle = Bundle()
                bundle.putString(context.resources.getString(R.string.tag_name), binding.nameText.text.toString())
                bundle.putString(context.resources.getString(R.string.tag_time), binding.timeText.text.toString())
                bundle.putString(context.resources.getString(R.string.tag_id), id)
                changeBusStop(bundle)
            }

            Log.d("deleteBusStop23", dataSet.size.toString())
            binding.apply {
                nameText.text = dataSet[position].busStop.name
                timeText.text = dataSet[position].time

                deleteBusStopButton.setOnClickListener {
                    deleteBusStop(id)
                }

                nameText.setOnClickListener { changeBusStopData() }
                timeText.setOnClickListener { changeBusStopData() }

            }
        }

    }
}