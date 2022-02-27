package com.dru128.timetable.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.worker.map.BusStopsRecycleAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dru128.timetable.R


class BusStopsBottomSheet(var index: Int, busStopWiths: List<BusStopWithTime>): BottomSheetDialogFragment()
{
    private var adapter = BusStopsRecycleAdapter(index, busStopWiths, ::onItemClick)
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.bottom_sheet_fragment, container, false)

        recyclerView = root.findViewById(R.id.recylerView_bottom_Sheet)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return root
    }

    private fun onItemClick(i: Int)
    {
        val bundle = Bundle()
        bundle.putInt(requireContext().getString(R.string.busStop_id), i)

        parentFragmentManager.setFragmentResult(
            requireContext().getString(R.string.itemSelected), bundle
        )
        dismiss()
    }
}

