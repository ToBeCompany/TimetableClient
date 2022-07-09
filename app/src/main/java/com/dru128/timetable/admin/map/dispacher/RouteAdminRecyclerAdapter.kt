package com.dru128.timetable.admin.map.dispacher

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
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

            val mapboxRoute = RouteAdminStorage.mapboxRoutes[ dataSet[position].route.id ]
            if (mapboxRoute == null)
                isShowRoute(false, position)
            else
                isShowRoute(mapboxRoute.isVisible, position)


            binding.visibleRoute.setOnCheckedChangeListener { v, isChecked ->
                Log.d("setOnCheckedChangeListener", isChecked.toString())
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
            Log.d("isShowRoute", isShow.toString())
            if (isShow)
            {
                showRoute(dataSet[i].route)
                binding.root.transitionAlpha = 1f
//                binding.root.setCardBackgroundColor( Color.WHITE)
                binding.visibleRoute.apply {
                    setBackgroundResource(R.drawable.visibility)
                    if (!isChecked) isChecked = true
                }
            }
            else
            {
                hideRoute(dataSet[i].route.id)
                binding.root.transitionAlpha = 0.7f
//                binding.root.setCardBackgroundColor( Color.GRAY)
                binding.visibleRoute.apply {
                    setBackgroundResource(R.drawable.visibility_off)
                    if (isChecked) isChecked = false
                }
            }
        }

    }
}