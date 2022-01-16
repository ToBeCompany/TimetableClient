package com.example.timetable.driver

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.example.timetable.R
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import com.example.timetable.data.response.FlightsNameResponse
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timetable.databinding.FragmentDriverBinding
import com.example.timetable.driver.service.DriverService
import com.example.timetable.driver.service.Notification
import kotlinx.coroutines.flow.collect


class DriverFragment : Fragment(R.layout.fragment_driver)
{
    private val viewModel: DriverViewModel by viewModels()
    private val PERMISSION_CODE = 200

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var isListiningPos = false

    private lateinit var binding: FragmentDriverBinding

    private var adapter = RecyclerAdapterFlightNames (::selectRoute)

    private var routeId: String? = null

    fun selectRoute(selectedRoute: FlightsNameResponse)
    {
        if (selectedRoute.id != null && selectedRoute.name != null)
        {
            if (routeId != null)
            {
                viewModel.stopSearch(requireContext())
                viewModel.startSearch(requireContext(), selectedRoute.id!!)
            }
            routeId = selectedRoute.id!!
            binding.selectedRouteFragmentDriver.text = selectedRoute.name!!
            binding.ButtonTrackerFragmentDriver.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadFlight()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDriverBinding.bind(view)

        binding.ButtonTrackerFragmentDriver.setOnClickListener {
//            checkLocationPermissions()
            val intent = Intent(requireContext(), DriverService::class.java)
            startForegroundService(requireContext(),intent)

        }
        binding.recyclerviewRoutesFragmentDriver.adapter = adapter
        binding.recyclerviewRoutesFragmentDriver.layoutManager = LinearLayoutManager(context)


        lifecycleScope.launchWhenStarted {
            viewModel.flightName.collect {
                if (it.isNotEmpty())
                {
                    Log.d("ResponseServer", "data (flight) Ready")
                    adapter.dataset = it
                    adapter.notifyDataSetChanged()
                    binding.progressFragmentDriver.visibility = View.GONE
                }
                else
                {
                    Log.d("ResponseServer", "data (flight) is null")
                }
            }
        }
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
                    binding.ButtonTrackerFragmentDriver.text = getString(R.string.on_tracker)
                    viewModel.stopSearch(requireContext())
                }
                else
                {
                    binding.ButtonTrackerFragmentDriver.text = getString(R.string.off_tracker)
                    viewModel.startSearch(requireContext(), routeId!!)
                }

                isListiningPos = !isListiningPos
            }
        else
        {
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
        }
    }
}