package com.dru128.timetable.worker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.Repository
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.worker.map.RouteWorkerStorage
import dru128.timetable.R


class RouteRecyclerAdapter(var click: (id: Int) -> Unit, var dataSet: Array<Route> = arrayOf())
    : RecyclerView.Adapter<RouteRecyclerAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false) // тут item
        return ViewHolder(View)
    }

    fun updateData()
    {
        dataSet = RouteWorkerStorage.routes
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context

        var routeNameText = itemView.findViewById<TextView>(R.id.nameRoute_route_item)

        fun onBind(position: Int)
        {
            routeNameText.text = dataSet[position].name
            itemView.setOnClickListener {
                click(position)
            }
        }
    }
}