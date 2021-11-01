package com.example.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    class RecyclerAdapterTypesHelpAdmin: RecyclerView.Adapter<RecyclerAdapterTypesHelpAdmin.ViewHolder>()
    {
        var dataset: MutableList<String> = mutableListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val View = LayoutInflater.from(parent.context).inflate(R.layout.type_help_item_admin, parent, false)
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
            var type_text = itemView.findViewById<TextView>(R.id.type_text)
            var deletetype_button = itemView.findViewById<Button>(R.id.delete_type_buton)

            fun onBind(type: String)
            {
                type_text.text = type
                deletetype_button.setOnClickListener {

                }
            }
        }
    }




}