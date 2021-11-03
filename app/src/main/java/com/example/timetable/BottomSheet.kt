package com.example.timetable


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.data.BusStop
import com.example.timetable.data.Repository
import com.example.timetable.map.AdapterRecuclerMapView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheet(var index: Int, var curItem: Int) : BottomSheetDialogFragment()
{
    private var adapter = AdapterRecuclerMapView(Repository.busesData[index], curItem)
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.bottom_sheet_fragment, container, false)

        recyclerView = root.findViewById(R.id.ruculerView_bottom_Sheet)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return root
    }
}

