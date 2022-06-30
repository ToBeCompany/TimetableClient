package com.dru128.timetable.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.data.metadata.BusStopWithTime
import com.dru128.timetable.data.metadata.GeoPosition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dru128.timetable.R
import dru128.timetable.databinding.BottomSheetFragmentBinding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class BusStopsBottomSheet(idBusStop: String, var busStopWiths: List<BusStopWithTime>): BottomSheetDialogFragment()
{
    private var adapter = BusStopsRecycleAdapter(idBusStop, busStopWiths.toTypedArray(), ::onItemClick)
    private lateinit var binding: BottomSheetFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = BottomSheetFragmentBinding.inflate(inflater)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.smoothScrollToPosition(busStopWiths.size-1)

        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }
    private fun onItemClick(position: GeoPosition)
    {
        val bundle = Bundle()
        bundle.putString(requireContext().getString(R.string.busStop_id), Json.encodeToString(position))

        parentFragmentManager.setFragmentResult(
            requireContext().getString(R.string.itemSelected), bundle
        )
        dismiss()
    }
}

