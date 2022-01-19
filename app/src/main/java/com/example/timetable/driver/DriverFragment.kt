package com.example.timetable.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.timetable.R
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.metadata.response.FlightsNameResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.example.timetable.driver.service.DriverService

import android.app.ActivityManager


class DriverFragment : Fragment()
{
    private val viewModel: DriverViewModel by viewModels()
    private val PERMISSION_CODE = 200

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private lateinit var trackerButton: Button
    private lateinit var curRouteText: TextView
    private lateinit var parent: ConstraintLayout
    private lateinit var recyclerView: RecyclerView

    private var adapter = RecyclerAdapterFlightNames (::selectRoute)

    private var selectedRouteId: String? = null
    private var isListiningPos = false

    fun selectRoute(selectedRoute: FlightsNameResponse)
    {
        if (selectedRoute.id != null && selectedRoute.name != null)
        {
//            if (routeId != null && !isListiningPos)
//            {
//                requireContext().startService(
//                    Intent(requireContext(), DriverService::class.java)
//                        .putExtra(getString(R.string.action), getString(R.string.new_tracker))
//                        .putExtra(getString(R.string.tracker_id), selectedRoute.id!!)
//                )
//            }
            selectedRouteId = selectedRoute.id!!
            curRouteText.text = selectedRoute.name!!
            trackerButton.isEnabled = true
            trackerButton.text = getString(R.string.on_tracker)
            isListiningPos = false

            if (isMyServiceRunning(DriverService::class.java))
            {
                requireContext().startService(
                    Intent(requireContext(), DriverService::class.java)
                        .putExtra(getString(R.string.action), getString(R.string.off_service))
                )

                Snackbar.make(requireView(), getString(R.string.racker_shutdown), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_driver, container, false)


        trackerButton = root.findViewById(R.id.ButtonTracker_fragmentDriver)
        curRouteText = root.findViewById(R.id.selectedRoute_fragmentDriver)
        parent = root.findViewById(R.id.parent_fragmentDriver)
        recyclerView = root.findViewById(R.id.recyclerviewRoutes_fragmentDriver)


        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        if (isMyServiceRunning(DriverService::class.java))
        {
            isListiningPos = true
            trackerButton.text = getString(R.string.off_tracker)
        }
        else
        {
            trackerButton.text = getString(R.string.on_tracker)
            trackerButton.isEnabled = false
        }

        viewModel.viewModelScope.launch {
            val response: List<FlightsNameResponse>? = viewModel.getFlight()
            if (response != null && response.isNotEmpty())
            {
                Log.d("ResponseServer", "data (flight) Ready")

                adapter.dataset = response
                adapter.notifyDataSetChanged()

                parent.removeView(parent.findViewById(R.id.progress_fragmentDriver))
            }
            else
            {
                Log.d("ResponseServer", "data (flight) is null")
                // маршрутов нет ( 0 )
            }
        }

        trackerButton.setOnClickListener {
            checkLocationPermissions()
        }

        return root
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            checkLocationPermissions()
        else
            Snackbar.make(requireView(), getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkLocationPermissions()
    {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
            } else
            {
                if (isListiningPos)
                {
                    trackerButton.text = getString(R.string.on_tracker)
                    Snackbar.make(requireView(), getString(R.string.racker_shutdown), Snackbar.LENGTH_LONG).show()

                    requireContext().startService(
                        Intent(requireContext(), DriverService::class.java)
                            .putExtra(getString(R.string.action), getString(R.string.off_service))
                    )
                }
                else
                {
                    trackerButton.text = getString(R.string.off_tracker)
                    Snackbar.make(requireView(), getString(R.string.tracker_launched), Snackbar.LENGTH_LONG).show()

                    requireContext().startService(
                        Intent(requireContext(), DriverService::class.java)
                            .putExtra(getString(R.string.action), getString(R.string.on_service))
                            .putExtra(getString(R.string.tracker_id), selectedRouteId)
                    )
                }

                isListiningPos = !isListiningPos
            }
        else
        {
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean
    {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE))
        {
            if (serviceClass.name == service.service.className)
            {
                return true
            }
        }
        return false
    }

}