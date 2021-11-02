package com.example.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.data.BusData

class RecyclerAdapterMarshrut(var click: () -> Unit)
    : RecyclerView.Adapter<RecyclerAdapterMarshrut.ViewHolder>()
{
    var dataset: MutableList<BusData> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_view_marshrut , parent, false) //тут item
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(dataset[position])
    }

    override fun getItemCount(): Int = dataset.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var context = itemView.context

        var nameBaseText = itemView.findViewById<TextView>(R.id.textView)
//        var next_button = itemView.findViewById<Button>(R.id.btnOf_Item)


        fun onBind(data: BusData)
        {

            nameBaseText.text = data.name
//            next_button.setOnClickListener {
//                click()
//            }
            itemView.setOnClickListener {
                click()
            }



            //                itemView.findNavController()
//                        .navigate(R.id.action_mainFragment_to_fragmentMap)

        }
    }
}