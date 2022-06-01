package com.dru128.timetable.admin.edituser

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import dru128.timetable.R
import dru128.timetable.databinding.AddUserDialogBinding


class AddUserDialog: DialogFragment(/*R.layout.add_user_dialog*/)
{
    private var addUser: CreateUser? = null

    private lateinit var binding: AddUserDialogBinding
    val typeUserConvertor = mapOf<String, TypeUser>(
        "Работник" to TypeUser.WORKER,
        "Водитель" to TypeUser.DRIVER,
        "Администратор" to TypeUser.ADMIN
    )
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        initSpinner()

        binding.autoGenerationCheckbox.setOnCheckedChangeListener { v, isChecked ->
            if (isChecked)
                binding.idUser.visibility = View.GONE
            else
                binding.idUser.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = AddUserDialogBinding.inflate(layoutInflater)
        addUser = requireParentFragment() as CreateUser

        val builder = AlertDialog.Builder(activity)
        return builder
            .setView(binding.root)
            .setNegativeButton( requireContext().resources.getString(R.string.close) )
            { _dialog, _id -> onCancel(_dialog) }
            .setPositiveButton( requireContext().resources.getString(R.string.add) )
            { _dialog, _id ->
                val userId =
                    if (binding.autoGenerationCheckbox.isChecked) ""
                    else binding.idUser.text.toString()
                val user = User(
                    userType = typeUserConvertor[binding.typeNewUser.selectedItem]!!,
                    id = userId
                )
                addUser?.createUser(user)
            }
            .create()
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

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(false)
    }
}