package com.example.timetable.worker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.data.metadata.Route
import com.example.timetable.data.metadata.response.FlightsNameResponse


class RecyclerAdapterRoute(var click: (id:String) -> Unit)
    : RecyclerView.Adapter<RecyclerAdapterRoute.ViewHolder>()
{
    var dataset: List<Route> = Storage.routes
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false) // тут item
        return ViewHolder(View)
    }

    fun updateData()
    {
        dataset = Storage.routes
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataset.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context

        var routeNameText = itemView.findViewById<TextView>(R.id.nameRoute_route_item)

        fun onBind(position: Int)
        {

            routeNameText.text = dataset[position].name
            itemView.setOnClickListener {
                click(dataset[position].id)
            }
        }
    }
}