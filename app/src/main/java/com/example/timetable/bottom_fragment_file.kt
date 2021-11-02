package com.example.timetable


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.map.AdapterRecuclerMapView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class bottom_fragment_file : BottomSheetDialogFragment(){


    private var adapter = AdapterRecuclerMapView{  bottom_fragment_file().show(requireFragmentManager(),"BottomSheetDialog")}
    private var recyclerView: RecyclerView? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_map, container, false)
        var button = root.findViewById<Button>(R.id.buttontest2)

        recyclerView = root.findViewById(R.id.recucler_View_Mainfrag)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        var btn1 = view.findViewById<Button>(R.id.btn_bottom2)
//        var btn2 = view.findViewById<Button>(R.id.btn_bottom)
//        btn1.setOnClickListener(){
//            Toast.makeText(context,"1",Toast.LENGTH_SHORT).show()
//        }
//        btn2.setOnClickListener(){
//            Toast.makeText(context,"2",Toast.LENGTH_SHORT).show()
//        }
    }

}

