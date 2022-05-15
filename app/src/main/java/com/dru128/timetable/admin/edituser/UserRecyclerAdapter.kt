package com.dru128.timetable.admin.edituser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.User
import dru128.timetable.databinding.UserItemBinding


class UserRecyclerAdapter(var click: (id: String) -> Unit, var dataSet: MutableList<User> = mutableListOf())
    : RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context

        var userRole = binding.userRole
        var userId = binding.userId
        var deleteUserButton = binding.userDelete

        fun onBind(position: Int)
        {
            userRole.text = dataSet[position].userType.toString()
            userId.text = dataSet[position].id

            deleteUserButton.setOnClickListener {
                click(dataSet[position].id)
            }
        }
    }
}