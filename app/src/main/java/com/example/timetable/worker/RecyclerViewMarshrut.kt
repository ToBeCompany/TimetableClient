package com.example.timetable.worker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.Storage
import com.example.timetable.data.Flight


class RecyclerAdapterMarshrut(var click: (id:Int) -> Unit)
    : RecyclerView.Adapter<RecyclerAdapterMarshrut.ViewHolder>()
{
    var dataset: MutableList<Flight> = Storage.flights
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_view_marshrut, parent, false) // тут item
        return ViewHolder(View)
    }

    fun updateData()
    {
        dataset = Storage.flights
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

        var nameBaseText = itemView.findViewById<TextView>(R.id.textView)

        fun onBind(position: Int)
        {

            nameBaseText.text = dataset[position].route.name
            itemView.setOnClickListener {
                click(position)
            }
        }
    }
}