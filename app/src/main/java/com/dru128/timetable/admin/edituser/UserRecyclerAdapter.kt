package com.dru128.timetable.admin.edituser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.UserItemBinding


class UserRecyclerAdapter(var deleteUser: (user: User) -> Unit, var dataSet: Array<User>)
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
            userId.text = dataSet[position].id
            userRole.text = when (dataSet[position].userType)
            {
                TypeUser.WORKER -> context.resources.getString(R.string.worker)
                TypeUser.DRIVER -> context.resources.getString(R.string.driver)
                TypeUser.ADMIN -> context.resources.getString(R.string.admin)
                else-> context.resources.getString(R.string.error)
            }

            deleteUserButton.setOnClickListener {
                deleteUser(dataSet[position])
            }
            userId.setOnClickListener {
                Snackbar.make(it, context.resources.getString(R.string.id_is_coped), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}