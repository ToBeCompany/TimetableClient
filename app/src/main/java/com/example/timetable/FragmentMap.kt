package com.example.timetable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FragmentMap : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        var root =  inflater.inflate(R.layout.fragment_map, container, false)

    val recyclerView: RecyclerView = root.findViewById(R.id.recyclerViewTimeTable)
    recyclerView.layoutManager = LinearLayoutManager(context)





        return root
    }






}