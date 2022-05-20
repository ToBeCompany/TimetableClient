package com.dru128.timetable.admin.edituser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentEditUsersBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class EditUsersFragment : Fragment()
{
    private lateinit var binding: FragmentEditUsersBinding
    private val viewModel: EditUsersViewModel by viewModels()



    private var adapter: UserRecyclerAdapter? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentEditUsersBinding.inflate(inflater)

        binding.addUserButton.setOnClickListener {
            AddUserDialog().show(childFragmentManager, AddUserDialog.TAG)
        }
        initRecyclerView()

        return binding.root
    }

    private fun initRecyclerView()
    {
        lifecycleScope.launch {
            adapter = UserRecyclerAdapter( { user -> deleteUser(user) }, viewModel.getUsers())
            recyclerView = binding.userRecyclerView
            recyclerView?.adapter = adapter
            recyclerView?.layoutManager = LinearLayoutManager(context)
            var a = arrayOf<User>()
            for (i in 0..20) a += User(TypeUser.WORKER, null, i.toString())
            viewModel.userList.value = a
            adapter?.dataSet = a
            adapter?.notifyDataSetChanged()
            viewModel.userList.collect { newData ->
                adapter?.dataSet = newData
//                adapter?.notifyDataSetChanged()
                Log.d("userList", "UDDATE22")

            }
        }
    }

    fun deleteUser(user: User)
    {
        Log.d("request", "delete user by id = ${user.id}")

        lifecycleScope.launch {
            val status = viewModel.deleteUser(user.id)
            Log.d("status", "= $status")
            if (status)
            {
                val users = viewModel.userList.value
                val position = users.indexOf(user)

                users
                    .filter { it.id != user.id }
                    .let { viewModel.userList.value = it.toTypedArray() }

                adapter?.apply {
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position,  users.size)
                }
            }
            else
                Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_delete_user), Snackbar.LENGTH_LONG).show()
        }
    }
}