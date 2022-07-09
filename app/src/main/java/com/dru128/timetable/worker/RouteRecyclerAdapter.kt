package com.dru128.timetable.worker

import android.view.LayoutInflater
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.worker.map.RouteWorkerStorage
import dru128.timetable.databinding.RouteItemBinding


class RouteRecyclerAdapter(
    var click: (id: String) -> Unit,
    var longClick: (id: String) -> Unit,
    var dataSet: Array<Route> = arrayOf()
) : RecyclerView.Adapter<RouteRecyclerAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = RouteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(var binding: RouteItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context

        fun onBind(position: Int)
        {
            binding.routeName.text = dataSet[position].name
            binding.root.setOnClickListener {
                click(dataSet[position].id)
            }
            binding.root.setOnLongClickListener {
                longClick(dataSet[position].id)
                true
            }
        }
    }
}