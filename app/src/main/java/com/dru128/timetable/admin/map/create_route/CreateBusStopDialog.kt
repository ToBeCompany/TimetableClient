package com.dru128.timetable.admin.map.create_route

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.EditBusStopDialogBinding


internal interface CreateBusStopFromDialog { fun createNewBusStop(name: String, time: String) }
class CreateBusStopDialog: DialogFragment()
{
    private lateinit var binding: EditBusStopDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val createBusStop = requireParentFragment() as CreateBusStopFromDialog

        binding = EditBusStopDialogBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(activity)
        return builder
            .setView(binding.root)
            .setTitle(requireContext().resources.getString(R.string.bus_stop_data))
            .setNegativeButton( requireContext().resources.getString(R.string.close) )
            { _dialog, _id -> onCancel(_dialog) }
            .setPositiveButton( requireContext().resources.getString(R.string.save) )
            { _dialog, _id ->
                val newName = binding.name.text.toString()
                val newTime = binding.time.text.toString()
                if (newName.isNotBlank() && newTime.isNotBlank())
                {
                    createBusStop.createNewBusStop(newName, newTime)
                }
                else
                    Snackbar.make(requireParentFragment().requireView(), requireContext().resources.getString(R.string.fill_all_fields), Snackbar.LENGTH_SHORT).show()
            }
            .create()
    }

    companion object {
        const val TAG = "CreateBusStopDialog"
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(false)
    }
}