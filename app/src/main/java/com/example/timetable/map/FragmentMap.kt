package com.example.timetable.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.BottomSheet
import com.google.android.gms.maps.SupportMapFragment

class FragmentMap: Fragment()
{
//    private var bottomSheet = BottomSheet()

    private var recyclerView: RecyclerView? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_map, container, false)
//        var button = root.findViewById<Button>(R.id.buttontest2)
        var bottomsheet_LinearLayout = root.findViewById<LinearLayout>(R.id.bottomsheet_LinearLayout)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }

}
