package com.dru128.timetable.admin.edituser

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.dru128.timetable.tools.IDManager
import dru128.timetable.R
import dru128.timetable.databinding.AddUserDialogBinding


interface CreateUser { fun createUser(user: User) }
class AddUserDialog: DialogFragment()
{
    private var addUser: CreateUser? = null

    private lateinit var binding: AddUserDialogBinding
    private val typeUserConvertor by lazy {
        mapOf<String, TypeUser>(
            getString(R.string.worker) to TypeUser.WORKER,
            getString(R.string.driver) to TypeUser.DRIVER,
            getString(R.string.admin) to TypeUser.ADMIN,
            getString(R.string.dispatcher) to TypeUser.DISPAT
        )
    }
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

        binding.createUserButton.setOnClickListener {

                if (binding.autoGenerationCheckbox.isChecked)
                {
                    addUser?.createUser(
                        User(
                            userType = typeUserConvertor[binding.typeNewUser.selectedItem]!!,
                            id = IDManager.generateID()
                        )
                    )
                    dismiss()
                } else
                {
                    val userId = binding.idUser.text.toString()
                    if (userId.length < 8 || userId.isBlank())
                        Toast.makeText(requireContext(), "Идентификатор должен быть не короче 8 символов!", Toast.LENGTH_LONG).show()
                    else if (IDManager.validateId(userId))
                    {
                        addUser?.createUser(
                            User(
                                userType = typeUserConvertor[binding.typeNewUser.selectedItem]!!,
                                id = userId
                            )
                        )
                        dismiss()
                    } else
                        Toast.makeText(requireContext(), "Идентификатор может содержать только латинские символы и цифры!", Toast.LENGTH_LONG).show()

                }
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        val builder = AlertDialog.Builder(activity)
        return builder
            .setView(binding.root)
            .setTitle(requireContext().resources.getString(R.string.user_data))
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
}