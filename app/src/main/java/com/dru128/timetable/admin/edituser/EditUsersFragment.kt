package com.dru128.timetable.admin.edituser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.dru128.timetable.data.metadata.type_user_creater
import com.dru128.timetable.worker.RouteFragmentDirections
import com.dru128.timetable.worker.RouteRecyclerAdapter
import dru128.timetable.databinding.FragmentEditUsersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class EditUsersFragment : Fragment()
{
    private lateinit var binding: FragmentEditUsersBinding
    private val viewModel: EditUsersViewModel by viewModels()

    val typeUserConvertor = mapOf<String, TypeUser>(
        "Работник" to TypeUser.WORKER,
        "Водитель" to TypeUser.DRIVER,
        "Администратор" to TypeUser.ADMIN
    )

    private var adapter = UserRecyclerAdapter(
        { id -> deleteUser(id) },
        mutableListOf()
    )
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentEditUsersBinding.inflate(inflater)

        initRecyclerView()

//        initSpinner()
//
//        binding.createUserButton.setOnClickListener {
//            lifecycleScope.launch{
//                viewModel.createUser(
//                    User(
//                        userType = type_user_creater(binding.typeNewUser.selectedItem.toString()),
//                        id = binding.idNewUser.text.toString()
//                    )
//                )
//            }
//        }
//
//        binding.deleteUserButton.setOnClickListener {
//            lifecycleScope.launch{
//                viewModel.deleteUser(binding.DeleteIdText.toString())
//            }
//        }
        return binding.root
    }

    private fun initRecyclerView()
    {
        lifecycleScope.launch {
            recyclerView = binding.userRecyclerView
            recyclerView?.adapter = adapter
            for (i in 0..9)
                adapter.dataSet.add(User(TypeUser.WORKER, null, i.toString()))

            recyclerView?.layoutManager = LinearLayoutManager(context)
        }
    }

//    private fun initSpinner()
//    {
//        var userTypes = arrayOf<String>()
//        for ((key, value) in typeUserConvertor) userTypes += key
//
//        val adapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item,
//            userTypes
//        )
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.typeNewUser.adapter = adapter
//
//    }

    fun deleteUser(id: String)
    {
        Log.d("request", "delete user by id = $id")
        lifecycleScope.launch {
            var isSuccess = viewModel.deleteUser(id)
            if (isSuccess)
            {
                adapter.dataSet.removeAt(position) // удалить пользователя из списка данных (dataset)
                adapter.notifyDataSetChanged()
            }
        }
    }
}