package com.dru128.timetable.worker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.Storage
import com.dru128.timetable.data.metadata.Route
import dru128.timetable.R
import com.dru128.timetable.tools.ProgressManager
import kotlinx.coroutines.launch


class RouteFragment : Fragment()
{
    private val viewModel: RouteViewModel by viewModels()

    private lateinit var progressManager: ProgressManager

    private var adapter = RouteRecyclerAdapter(
        { id -> findNavController().navigate(RouteFragmentDirections.actionRouteFragmentToMapsFragment(id)) },
        arrayOf()
    )
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        var root = inflater.inflate(R.layout.fragment_route, container, false)


        progressManager = ProgressManager(root.findViewById(R.id.parentMainFragment), requireActivity())
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
            val routes: Array<Route>? = viewModel.getRoutes()

            if (routes != null && routes.isNotEmpty())
            {
                progressManager.finish()

                Storage.routes = routes
                adapter.updateData()

            } else {
                // маршрутов нет ( 0 )
            }
            Log.d("data ready", "routes = ${routes?.size}")
        }
    }

}