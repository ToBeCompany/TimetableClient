package com.dru128.timetable.admin.map.dispacher

import android.content.Context
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


        var context: Context = binding.root.context

        fun onBind(position: Int)
        {

            initItem(dataSet[position].isVisible)

            binding.visibleRoute.setOnClickListener {
                Log.d("action", "checkbox click from adapter | id = ${dataSet[position].route.id}")
                isShowRoute(binding.visibleRoute.isChecked, position)
            }
            binding.deleteRoute.setOnClickListener {
                deleteRoute(dataSet[position].route.id)
            }
            binding.editRoute.setOnClickListener {
                editRoute(dataSet[position].route.id)
            }


        }

        private fun initItem(isShow: Boolean)
        {
            binding.nameRoute.text = dataSet[position].route.name

            binding.isOnline.visibility =
                if (dataSet[position].isOnline) View.VISIBLE
                else View.INVISIBLE

            if (isShow) shownMode()
            else hiddenMode()
        }

        private fun isShowRoute(isShow: Boolean, i: Int)
        {
            if (isShow)
            {
                Log.d("action", "show route from adapter")
                showRoute(dataSet[i].route)
                shownMode()
            }
            else
            {
                Log.d("action", "hide route from adapter")
                hideRoute(dataSet[i].route.id)
                hiddenMode()
            }
        }

        private fun hiddenMode()
        {
            binding.root.transitionAlpha = 0.7f
            binding.visibleRoute.apply {
                setBackgroundResource(R.drawable.visibility_off)
                if (isChecked) isChecked = false
            }
        }


        private fun shownMode()
        {
            binding.root.transitionAlpha = 1f
            binding.visibleRoute.apply {
                setBackgroundResource(R.drawable.visibility)
                if (!isChecked) isChecked = true
            }
        }

    }
}