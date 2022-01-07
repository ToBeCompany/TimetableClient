package com.example.timetable.auth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.Storage
import com.example.timetable.data.response.FlightsNameResponse

class RecyclerAdapterFlightNames()
    : RecyclerView.Adapter<RecyclerAdapterFlightNames.ViewHolder>()
{
    var dataset: List<FlightsNameResponse> = Storage.flightsNames

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapterFlightNames.ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_view_marshrut, parent, false) // тут item
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

        var flightNameText = itemView.findViewById<TextView>(R.id.name_flight_text)

        fun onBind(position: Int)
        {

            flightNameText.text = dataset[position].name
        }
    }



}