package com.example.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapterMarshrut: RecyclerView.Adapter<RecyclerAdapterMarshrut.ViewHolder>()
{
    var dataset: MutableList<String> = mutableListOf("foreasd", "jj" ,"asdaksjdbaksd" , "asdhagsjdasdasd adds asdasdasdq")
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
        var type_text = itemView.findViewById<TextView>(R.id.textView)
//        var deletetype_button = itemView.findViewById<Button>(R.id.delete_type_buton)

        fun onBind(type: String)
        {
            type_text.text = type
        }
    }
}