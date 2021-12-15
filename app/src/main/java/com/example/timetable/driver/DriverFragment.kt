package com.example.timetable.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import com.example.timetable.R
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import com.example.timetable.App
import com.example.timetable.MainActivity
import com.example.timetable.Resource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest


class DriverFragment : Fragment() 
{
    private val viewModel: DriverViewModel by viewModels()
    private val PERMISSION_CODE = 200

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_driver, container, false)
        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startSearch()
        }
//        root.findViewById<ToggleButton>(R.id.toggleTracker_fragmentDriver)
//            .setOnCheckedChangeListener { view, isChecked ->
//                checkLocationPermissions()
//                if (isChecked)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    {
//                    val intent = Intent(requireActivity(), DriverForeGroundTracker::class.java) // Build the intent for the service
//                        App.globalContext.startForegroundService(intent)
//                    }
//                }
//                else
//                {
//
//                }
//            }


        return root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            checkLocationPermissions()
        } else {
            Snackbar.make(requireView(), "getString(R.string.no_geo_position)", Snackbar.LENGTH_LONG)
                .show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkLocationPermissions() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_CODE
                )
            } else {
                lifecycle.coroutineScope.launchWhenStarted {
                    viewModel.startSearch()
                }
            } else {
            Snackbar.make(requireView(), "getString(R.string.gps_off)", Snackbar.LENGTH_LONG).show()
        }
    }

}