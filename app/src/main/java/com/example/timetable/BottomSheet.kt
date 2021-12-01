package com.example.timetable


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.data.Bus_Stop
import com.example.timetable.map.AdapterRecyclerMapView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheet(index: Int, busStops: MutableList<Bus_Stop>) : BottomSheetDialogFragment()
{
    private var adapter = AdapterRecyclerMapView(index, busStops)
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.bottom_sheet_fragment, container, false)

        recyclerView = root.findViewById(R.id.recylerView_bottom_Sheet)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return root
    }

    override fun onCancel(dialog: DialogInterface) {
        onDestroyView()
        super.onCancel(dialog)
    }
}

