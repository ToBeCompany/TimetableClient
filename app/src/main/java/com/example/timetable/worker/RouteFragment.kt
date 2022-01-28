package com.example.timetable.worker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.data.metadata.response.FlightsNameResponse
import com.example.timetable.system.ProgressManager
import kotlinx.coroutines.launch


class RouteFragment : Fragment()
{
    private val viewModel: RouteViewModel by viewModels()

    private lateinit var progressManager: ProgressManager

    private var adapter = RecyclerAdapterRoute{ id ->
        findNavController().navigate(
            RouteFragmentDirections.actionRouteFragmentToMapsFragment(id)
        )
    }
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_route, container, false)


        progressManager = ProgressManager(root.findViewById(R.id.parentMainFragment))
        progressManager.start()

        recyclerView = root.findViewById(R.id.recucler_View_Mainfrag)

        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)
        getData()

        return root
    }

    private fun getData() // получение данных и загрузка в список
    {
        viewModel.viewModelScope.launch {
            var flights: List<FlightsNameResponse>? = viewModel.getFlight()
            if (flights != null && flights.isNotEmpty())
            {
                Log.d("getdataServer", flights.toString())
                progressManager.finish()

                Storage.flightsNames = flights
                adapter.updateData()

            } else {
                // маршрутов нет ( 0 )
            }
        }
    }

}