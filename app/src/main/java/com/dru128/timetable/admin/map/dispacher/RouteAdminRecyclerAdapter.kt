package com.dru128.timetable.admin.map.dispacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.admin.map.RouteAdminStorage
import com.dru128.timetable.data.metadata.Route
import dru128.timetable.R
import dru128.timetable.databinding.RouteAdminItemBinding

class RouteAdminRecyclerAdapter(
    var showRoute: (route: Route) -> Unit,
    var hideRoute: (id: String) -> Unit,
    var deleteRoute: (id: String) -> Unit,
    var editRoute: (id: String) -> Unit,
    var dataSet: Array<DispatcherRouteItem>
) : RecyclerView.Adapter<RouteAdminRecyclerAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = RouteAdminItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    fun routePositionById(id: String): Int?
    {
        for (i in dataSet.indices)
            if (dataSet[i].route.id == id)
                return i
        return null
    }

    inner class ViewHolder(var binding: RouteAdminItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context

        fun onBind(position: Int)
        {
            binding.nameRoute.text = dataSet[position].route.name

            RouteAdminStorage.mapboxRoutes[dataSet[position].route.id]?.let { mapboxRoute ->
                if (mapboxRoute.isVisible)
                {
                    binding.root.alpha = 1.0f
                    binding.visibleRoute.apply {
                        setBackgroundResource(R.drawable.visibility)
                        if (!isChecked) isChecked = true
                    }
                }
            }

            binding.visibleRoute.setOnCheckedChangeListener { v, isChecked ->
                isShowRoute(isChecked, position)
            }
            binding.deleteRoute.setOnClickListener {
                deleteRoute(dataSet[position].route.id)
            }
            binding.editRoute.setOnClickListener {
                editRoute(dataSet[position].route.id)
            }


            binding.isOnline.visibility =
                if (dataSet[position].isOnline) View.VISIBLE
                else View.INVISIBLE
        }

        private fun isShowRoute(isShow: Boolean, i: Int)
        {
            if (isShow)
            {
                showRoute(dataSet[i].route)
                binding.root.alpha = 1.0f
                binding.visibleRoute.apply {
                    setBackgroundResource(R.drawable.visibility)
                    if (!isChecked) isChecked = true
                }
            }
            else
            {
                hideRoute(dataSet[i].route.id)
                binding.root.alpha = 0.7f
                binding.visibleRoute.apply {
                    setBackgroundResource(R.drawable.visibility_off)
                    if (isChecked) isChecked = false
                }
            }
        }

    }
}