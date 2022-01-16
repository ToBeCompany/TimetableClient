package com.example.timetable.worker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetable.R
import com.example.timetable.data.metadata.Route
import com.example.timetable.data.metadata.response.FlightsNameResponse
import kotlinx.coroutines.launch


class RouteFragment : Fragment()
{
    private val viewModel: RouteViewModel by viewModels()

    private var adapter = RecyclerAdapterRoute{ id ->
        findNavController().navigate(
            RouteFragmentDirections.actionRouteFragmentToMapsFragment(id)
        )
    }
    private var recyclerView: RecyclerView? = null
    private var parent: ConstraintLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_route, container, false)



        parent = root.findViewById(R.id.parentMainFragment)
        recyclerView = root.findViewById(R.id.recucler_View_Mainfrag)

        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)
        getData()

        return root
    }

    private fun getData() // получение данных и загрузка в список
    {
        viewModel.viewModelScope.launch {
            if (Storage.routes.isNullOrEmpty())
            {
                val flights: List<Route>? = viewModel.getRoutes()
                if (!flights.isNullOrEmpty())
                {
                    Log.d("getData", flights.toString())


                    parent?.removeView(parent?.findViewById(R.id.progressMainFragment))
                    Storage.routes = flights
                    adapter.updateData()

                } else {
                    Log.d("getData", "routes = 0")

                    // маршрутов нет ( 0 )
                }
            }
        }
    }

}