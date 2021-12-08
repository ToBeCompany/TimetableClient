package com.example.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.data.BusData
import com.example.timetable.data.Repository

class RecyclerAdapterMarshrut(var click: (id:Int) -> Unit)
    : RecyclerView.Adapter<RecyclerAdapterMarshrut.ViewHolder>()
{
    var dataset: MutableList<BusData> = Repository.busesData
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_view_marshrut, parent, false) // тут item
        return ViewHolder(View)
    }

    fun updateData()
    {
        dataset = Repository.busesData
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(dataset[position], position)
    }

    override fun getItemCount(): Int = dataset.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context

        var nameBaseText = itemView.findViewById<TextView>(R.id.textView)
//        var next_button = itemView.findViewById<Button>(R.id.btnOf_Item)


        fun onBind(data: BusData, position: Int)
        {

            nameBaseText.text = data.name.toString()
            itemView.setOnClickListener {
                click(position)
            }
        }
    }
}