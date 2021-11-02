//package com.example.timetable
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.timetable.map.AdapterRecuclerMapView
//
//class FragmentMap: Fragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        var root = inflater.inflate(R.layout.fragment_map, container, false)
//
//        var btnsheet = root.findViewById<Button>(R.id.btn_start_sheet)
//        val bottomSheet = bottom_fragment_file()
//        btnsheet.setOnClickListener() {
//            bottomSheet.show(requireFragmentManager(),"BottomSheetDialog")
//
//        }
//
//        var adapter = AdapterRecuclerMapView()
//        var recyclerView = root.findViewById<RecyclerView>(R.id.recyclerViewTimeTable)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        return root
//    }
//}