package com.example.timetable.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R

class AdapterRecuclerMapView(var clickItemMap: () -> Unit)
    : RecyclerView.Adapter<AdapterRecuclerMapView.ViewHolder>()
{
    var dataset: MutableList<TypeMap> = mutableListOf(TypeMap("1-ый", "9:30"),TypeMap("2-ой", "10:30"))
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
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
        var type_text = itemView.findViewById<TextView>(R.id.textViewOst)
        var type_text_time = itemView.findViewById<TextView>(R.id.textViewTime)
        var itemMap = itemView.findViewById<ConstraintLayout>(R.id.constrainOf_Item_map)

        fun onBind(type: TypeMap)
        {
            type_text.text = type.typeOst
            type_text_time.text = type.typeTime
            itemMap.setOnClickListener(){
                clickItemMap()
            }

        }
    }

}