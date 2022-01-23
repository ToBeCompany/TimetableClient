package com.example.timetable.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.data.metadata.response.FlightsNameResponse


class RecyclerAdapterFlightNames(var click: (curItem: FlightsNameResponse) -> Unit)
    : RecyclerView.Adapter<RecyclerAdapterFlightNames.ViewHolder>()
{
    var dataset: List<FlightsNameResponse> = listOf()

    fun updateData(newData: List<FlightsNameResponse>)
    {
        dataset = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false) // тут item
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataset.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context

        var flightNameText = itemView.findViewById<TextView>(R.id.nameRoute_route_item)

        fun onBind(position: Int)
        {

            itemView.setOnClickListener {
                click(dataset[position])
            }
            flightNameText.text = dataset[position].name
        }
    }



}