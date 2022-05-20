package com.dru128.timetable.admin.edituser

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = AddUserDialogBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(activity)
        return builder
            .setView(binding.root)
            .setNegativeButton( requireContext().resources.getString(R.string.close) )
            { dialog, id -> onCancel(dialog) }
            .setPositiveButton( requireContext().resources.getString(R.string.add) )
            { dialog, id ->
                var id =
                    if (binding.autoGenerationCheckbox.isChecked) binding.idUser.text.toString()
                    else ""
                val user = User(
                    userType = typeUserConvertor[binding.typeNewUser.selectedItem]!!,
                    id = id
                )
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