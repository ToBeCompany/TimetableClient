package com.example.timetable.driver

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import com.example.timetable.R
import android.widget.Toast
import com.example.timetable.App
import com.example.timetable.MainActivity


class DriverFragment : Fragment() 
{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_driver, container, false)

        root.findViewById<ToggleButton>(R.id.toggleTracker_fragmentDriver)
            .setOnCheckedChangeListener { view, isChecked ->
                if (isChecked)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                    val intent = Intent(requireActivity(), DriverForeGroundTracker::class.java) // Build the intent for the service
                        App.globalContext.startForegroundService(intent)
                    }
                }
                else
                {

                }
            }

        return root
    }

}