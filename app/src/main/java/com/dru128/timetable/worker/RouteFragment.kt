package com.dru128.timetable.worker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.data.metadata.Route
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.worker.map.RouteWorkerStorage
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentRouteBinding
import kotlinx.coroutines.launch


class RouteFragment : Fragment()
{
    private lateinit var binding: FragmentRouteBinding
    private val viewModel: RouteViewModel by viewModels()

    private var adapter = RouteRecyclerAdapter(
        { id -> findNavController().navigate(RouteFragmentDirections.actionRouteFragmentToMapsFragment(id)) },
        { id ->
            viewModel.addToFavourite(id)
            initRecyclerViews()
        },
        arrayOf()
    )

    private var favouriteAdapter = RouteRecyclerAdapter(
        { id -> findNavController().navigate(RouteFragmentDirections.actionRouteFragmentToMapsFragment(id)) },
        { id ->
            viewModel.deleteFromFavourite(id)
            initRecyclerViews()
        },
        arrayOf()
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentRouteBinding.inflate(inflater)
        super.onViewCreated(binding.root, savedInstanceState)
        val progressManager = ProgressManager(binding.parent, requireActivity())
        progressManager.start()


        binding.routesRecyclerView.adapter = adapter
        binding.favouriteRoutesRecyclerView.adapter = favouriteAdapter
        binding.routesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.favouriteRoutesRecyclerView.layoutManager = LinearLayoutManager(context)

        getData()

        progressManager.finish()
        return binding.root
    }

    private fun getData() // получение данных и загрузка в список
    {
        lifecycleScope.launch {
            val routes: Array<Route>? = viewModel.getRoutes()

            if (routes != null && routes.isNotEmpty())
            {
                RouteWorkerStorage.routes = routes
                initRecyclerViews()
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.pinch_to_add_to_favorites), Toast.LENGTH_SHORT).show()

            } else {
                // не получилось получить маршруты
            }
            Log.d("data ready", "routes = ${routes?.size}")
        }
    }

    private fun initRecyclerViews()
    {
        Log.d("event", "initRecyclerViews")

        var routes = arrayOf<Route>()
        var favouriteRoutes = arrayOf<Route>()

        viewModel.getFavouriteRoutes().let { favourites ->
            if (favourites.isNotEmpty() && viewModel.isFavouritesContainsRealRoutes(RouteWorkerStorage.routes, favourites))
            {
                binding.favouriteRouteLayout.visibility = View.VISIBLE
                RouteWorkerStorage.routes.forEach { _route ->
                    if (favourites.contains(_route.id))
                        favouriteRoutes += _route
                    else
                        routes += _route
                }
            }
            else
            {
                binding.favouriteRouteLayout.visibility = View.GONE
                routes = RouteWorkerStorage.routes
            }
        }

        adapter.dataSet = routes
        favouriteAdapter.dataSet = favouriteRoutes
        adapter.notifyDataSetChanged()
        favouriteAdapter.notifyDataSetChanged()
    }
}