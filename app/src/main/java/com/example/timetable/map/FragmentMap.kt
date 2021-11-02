package com.example.timetable.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.bottom_fragment_file
import com.example.timetable.data.BusData
import com.example.timetable.data.BusStop
import com.example.timetable.firebase.BusFireBase
import com.google.firebase.firestore.GeoPoint

class FragmentMap: Fragment()
{

//    private var bottomSheet = bottom_fragment_file()
//    private var adapter = AdapterRecuclerMapView{  bottom_fragment_file().show(requireFragmentManager(),"BottomSheetDialog")}
//    private var recyclerView: RecyclerView? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_map, container, false)
        var button = root.findViewById<Button>(R.id.buttontest2)
//
//        recyclerView = root.findViewById(R.id.recucler_View_Mainfrag)
//        recyclerView?.adapter = adapter
//        recyclerView?.layoutManager = LinearLayoutManager(context)

        return root
    }

}