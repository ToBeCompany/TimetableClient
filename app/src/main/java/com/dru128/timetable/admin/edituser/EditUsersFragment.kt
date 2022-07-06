package com.dru128.timetable.admin.edituser

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentEditUsersBinding
import kotlinx.coroutines.launch


class EditUsersFragment : Fragment(), CreateUser
{
    private lateinit var binding: FragmentEditUsersBinding
    private val viewModel: EditUsersViewModel by viewModels()

    private var adapter: UserRecyclerAdapter = UserRecyclerAdapter(
        { user -> deleteUser(user) },
        arrayOf()
    )

    val typeUserConvertor = mapOf<String, TypeUser?>(
        "Все" to null,
        "Работник" to TypeUser.WORKER,
        "Водитель" to TypeUser.DRIVER,
        "Админ" to TypeUser.ADMIN
    )
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentEditUsersBinding.inflate(inflater)


        binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.userRecyclerView.adapter = adapter

        binding.createUserButton.setOnClickListener {
            Log.d("event", "show dialog create user")
            val createUserDialog = AddUserDialog()
            createUserDialog.isCancelable = false
            createUserDialog.show(childFragmentManager, AddUserDialog.TAG)


        }

        binding.sortByIdText.addTextChangedListener { _id ->
            if (_id.isNullOrEmpty())
                binding.resetIdText.visibility = View.GONE
            else
                binding.resetIdText.visibility = View.VISIBLE
            adapter.sortById(_id.toString(), UsersStorage.userList.value)
        }

        binding.resetIdText.setOnClickListener {
            binding.sortByIdText.setText("")
        }

        binding.sortByRoleSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long)
            {
                adapter.sortByRole(typeUserConvertor[parentView?.selectedItem], UsersStorage.userList.value)
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        initSpinner()

        return binding.root
    }

    override fun onAttach(context: Context) {
        initRecyclerView()
        super.onAttach(context)
    }

    private fun initRecyclerView()
    {
        if (UsersStorage.userList.value.isEmpty())
        {
            Log.d("request", "get users from server")
            lifecycleScope.launch {
                val status: Boolean = viewModel.getUsers()
                Log.d("status", "= $status")
                if (status)
                {
                    adapter.dataSet = UsersStorage.userList.value
                    adapter.notifyDataSetChanged()
                }
//                else
//                    Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_get_users), Snackbar.LENGTH_LONG).show()
            }
        }
        else
        {
            Log.d("request", "get users from repository")
            adapter.dataSet = UsersStorage.userList.value
            adapter.notifyDataSetChanged()
        }
    }

    fun deleteUser(user: User)
    {
        Log.d("event", "show dialog delete user")
        AlertDialog.Builder(requireActivity())
            .setTitle(requireContext().resources.getString(R.string.delete_user))
//               .setIcon(R.drawable.hungrycat)
            .setNegativeButton(requireContext().resources.getString(R.string.cancel)) { dialog, _id -> dialog.dismiss() }
            .setPositiveButton(requireContext().resources.getString(R.string.confirm)) { dialog, _id ->
                Log.d("request", "delete user by id: ${user.id}")
                lifecycleScope.launch {
                    val status = viewModel.deleteUser(user.id)
                    Log.d("status", "= $status")
                    if (status)
                        adapter.sort(UsersStorage.userList.value)
                    else
                        Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_delete_user), Snackbar.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun createUser(user: User)
    {
        Log.d("request", "create user: $user")
        lifecycleScope.launch {
            val status = viewModel.createUser(user)
            Log.d("status", "= $status")
            if (status)
                adapter.sort(UsersStorage.userList.value)
            else
                Snackbar.make(binding.root, requireContext().resources.getString(R.string.error_create_user), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initSpinner()
    {
        var userTypes = arrayOf<String>()
        for ((key, value) in typeUserConvertor) userTypes += key

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            userTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortByRoleSpinner.adapter = adapter
    }

    override fun onResume()
    {
        adapter.sort(UsersStorage.userList.value)
        super.onResume()
    }

}