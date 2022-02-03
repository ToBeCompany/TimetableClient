package com.dru128.timetable.worker


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dru128.timetable.R
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.map.BusStopsRecycleAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BusStopsBottomSheet(index: Int, busStopWiths: List<BusStopWithTime>) : BottomSheetDialogFragment()
{
    private var adapter = BusStopsRecycleAdapter(index, busStopWiths)
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

