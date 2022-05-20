package com.dru128.timetable.admin.edituser

import android.app.Dialog
import android.os.Bundle
import android.text.BoringLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import dru128.timetable.R
import dru128.timetable.databinding.AddUserDialogBinding
import dru128.timetable.databinding.FragmentEditUsersBinding
import kotlinx.coroutines.launch

class AddUserDialog(var layout: Int = R.layout.user_item): DialogFragment(layout) // R.layout.user_item
{
    private lateinit var binding: AddUserDialogBinding
    val typeUserConvertor = mapOf<String, TypeUser>(
        "Работник" to TypeUser.WORKER,
        "Водитель" to TypeUser.DRIVER,
        "Администратор" to TypeUser.ADMIN
    )
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = AddUserDialogBinding.inflate(inflater)

        initSpinner()
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

        return binding.root
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {



        return super.onCreateDialog(savedInstanceState)
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
        binding.typeNewUser.adapter = adapter

    }

    companion object {
        const val TAG = "AddUserDialog"
    }
}