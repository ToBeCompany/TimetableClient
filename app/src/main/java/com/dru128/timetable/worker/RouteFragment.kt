package com.dru128.timetable.worker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.Repository
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.worker.map.RouteWorkerStorage
import dru128.timetable.databinding.FragmentRouteBinding
import kotlinx.coroutines.launch


class RouteFragment : Fragment()
{
    private lateinit var binding: FragmentRouteBinding
    private val viewModel: RouteViewModel by viewModels()

    private lateinit var progressManager: ProgressManager

    private var adapter = RouteRecyclerAdapter(
        { id -> findNavController().navigate(RouteFragmentDirections.actionRouteFragmentToMapsFragment(id)) },
        arrayOf()
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentRouteBinding.inflate(inflater)

        progressManager = ProgressManager(binding.parent, requireActivity())
        progressManager.start()


        binding.routesRecyclerView.adapter = adapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)
        getData()

        return binding.root
    }

    private fun getData() // получение данных и загрузка в список
    {
        lifecycleScope.launch {
            val routes: Array<Route>? = viewModel.getRoutes()

            if (routes != null && routes.isNotEmpty())
            {
                progressManager.finish()

                RouteWorkerStorage.routes = routes
                adapter.updateData()

            } else {
                // маршрутов нет ( 0 )
            }
            Log.d("data ready", "routes = ${routes?.size}")
        }
    }
}