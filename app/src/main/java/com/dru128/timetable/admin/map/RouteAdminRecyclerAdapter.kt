package com.dru128.timetable.admin.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.Route
import dru128.timetable.R
import dru128.timetable.databinding.RouteAdminItemBinding

class RouteAdminRecyclerAdapter(
    var showRoute: (route: Route) -> Unit,
    var hideRoute: (id: String) -> Unit,
    var dataSet: Array<Route>
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

    inner class ViewHolder(var binding: RouteAdminItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context

        fun onBind(position: Int)
        {
            binding.nameRoute.text = dataSet[position].name

            RouteAdminStorage.mapboxRoutes[dataSet[position].id]?.let { mapboxRoute ->
                isShowRoute(mapboxRoute.isVisible, position)
            }

            binding.visibleRoute.setOnCheckedChangeListener { v, isChecked ->
                isShowRoute(isChecked, position)
            }

      /*      dataSet[position].position.collect {

            }*/
        }

        private fun isShowRoute(isShow: Boolean, i: Int)
        {
            if (isShow)
            {
                showRoute(dataSet[i])
                binding.visibleRoute.setBackgroundResource(R.drawable.visibility)
                binding.root.alpha = 1.0f
                if (!binding.visibleRoute.isChecked) binding.visibleRoute.isChecked = true
            }
            else
            {
                hideRoute(dataSet[i].id)
                binding.visibleRoute.setBackgroundResource(R.drawable.visibility_off)
                binding.root.alpha = 0.7f
                if (binding.visibleRoute.isChecked) binding.visibleRoute.isChecked = false
            }
        }

    }
}