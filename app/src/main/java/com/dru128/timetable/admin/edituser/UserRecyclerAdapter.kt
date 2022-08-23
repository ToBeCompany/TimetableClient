package com.dru128.timetable.admin.edituser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.dru128.timetable.tools.Copy
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.UserItemBinding


class UserRecyclerAdapter(var deleteUser: (user: User) -> Unit, var dataSet: Array<User>)
    : RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>()
{
    private var filterId = ""
    private var filterRole: TypeUser? = null

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

    fun sortById(id: String, users: Array<User>) {
        filterId = id
        sort(users)
    }
    fun sortByRole(role: TypeUser?, users: Array<User>) {
        filterRole = role
        sort(users)
    }

    fun sort(users: Array<User>)
    {
        if (filterRole == null && filterId.isBlank())
            dataSet = users
        else
            users
                .filter { (it.userType == filterRole || filterRole == null) && it.id.contains(filterId) }
                .let { dataSet = it.toTypedArray() }
        notifyDataSetChanged()
    }

    inner class ViewHolder(var binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        var context = binding.root.context

        fun onBind(position: Int)
        {

            binding.userId.text = dataSet[position].id
            binding.userRole.text = when (dataSet[position].userType)
            {
                TypeUser.WORKER -> context.resources.getString(R.string.worker)
                TypeUser.DRIVER -> context.resources.getString(R.string.driver)
                TypeUser.ADMIN -> context.resources.getString(R.string.admin)
                TypeUser.DISPAT -> context.resources.getString(R.string.dispatcher)
                else-> context.resources.getString(R.string.error)
            }

            binding.userDelete.setOnClickListener {
                deleteUser(dataSet[position])
            }
            binding.userId.setOnClickListener {
                Copy.copyText(context, dataSet[position].id)
                Snackbar.make(it, context.resources.getString(R.string.id_is_coped), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}