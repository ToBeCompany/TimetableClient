package com.dru128.timetable.admin.map.create_route

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.EditBusstopDataDialogBinding

interface EditBusStopData { fun saveBusStopData(id: String?, name: String, time: String) }
class EditBusStopDataDialog: DialogFragment()
{
    private var save: EditBusStopData? = null

    private lateinit var binding: EditBusstopDataDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = EditBusstopDataDialogBinding.inflate(layoutInflater)
        save = requireParentFragment() as EditBusStopData
        binding.name.setText(
            arguments?.getString(requireContext().resources.getString(R.string.tag_name), "")
        )
        binding.time.setText(
            arguments?.getString(requireContext().resources.getString(R.string.tag_time), "")
        )

        binding.saveButton.setOnClickListener {
            val newName = binding.name.text.toString()
            val newTime = binding.time.text.toString()
            if (newName.isNotBlank() && newTime.isNotBlank())
            {
                save?.saveBusStopData(
                    arguments?.getString(requireContext().resources.getString(R.string.tag_id), null),
                    newName,
                    newTime
                )
                dismiss()
            }
            else
                Snackbar.make(requireParentFragment().requireView(), requireContext().resources.getString(R.string.fill_all_fields), Snackbar.LENGTH_SHORT).show()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        val builder = AlertDialog.Builder(activity)
        return builder
            .setView(binding.root)
            .setTitle(requireContext().resources.getString(R.string.bus_stop))
            .create()
    }



    companion object {
        const val TAG = "EditBusStopDataDialog"
    }

}