package com.example.timetable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.data.BusData
import com.example.timetable.data.BusStop
import com.example.timetable.firebase.BusFireBase
import com.google.firebase.firestore.GeoPoint


class MainFragment : Fragment()
{
    private var db = BusFireBase()
    private var adapter = RecyclerAdapterMarshrut()
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_main, container, false)

 //{ findNavController().navigate(R.id.action_mainFragment_to_fragmentMap) }
        recyclerView = root.findViewById(R.id.recucler_View_First)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)
        getData()

        return root
    }



    private fun getData()
    {
        db.ref
            .get()
            .addOnSuccessListener { result ->
                val data: MutableList<BusData> = mutableListOf()
                for (doc in result)
                    data.add(
                        BusData(
                            name = doc.get("name").toString(),
//                            uid = "0",
                            route = doc.get("route") as MutableList<GeoPoint>,
                            busStops = doc.get("busStops") as MutableList<BusStop>
                        )
                    )
                // здесь уже данные успешно получены
                adapter.dataset = data
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // всё плохо
                it.message.toString()
            }
    }
}