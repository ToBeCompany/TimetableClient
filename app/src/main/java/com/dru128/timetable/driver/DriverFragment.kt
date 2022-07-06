package com.dru128.timetable.driver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dru128.timetable.data.metadata.response.RouteNamesResponse
import com.dru128.timetable.driver.service.DriverService
import com.dru128.timetable.tools.ProgressManager
import com.dru128.timetable.tools.isServiceRunning
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentDriverBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class DriverFragment : Fragment()
{
    private lateinit var binding: FragmentDriverBinding
    private val viewModel: DriverViewModel by viewModels()
    private val PERMISSION_CODE = 200

    private val lastRoutePreference: LastRoutePreference by lazy {
        LastRoutePreference(requireContext())
    }

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private lateinit var progressManager: ProgressManager

    private var isListiningPos = false

    private var serviceReceiver: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            if (intent.hasExtra(getString(R.string.tracker_id)))
            {
                val jsonRoute = intent.getStringExtra(getString(R.string.tracker_id)).toString()
                val route: RouteNamesResponse = Json.decodeFromString(jsonRoute)
                Log.d("receiver_driver", jsonRoute)

                isListiningPos = true
                updateViews(route.name)
            } else if (intent.hasExtra(getString(R.string.off_service)))
            {
                isListiningPos = false
                updateViews(null)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            finishAffinity(requireActivity())
        }

        binding = FragmentDriverBinding.inflate(inflater)
        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(serviceReceiver, IntentFilter(getString(R.string.action)))

        progressManager = ProgressManager(binding.root, requireActivity())
        progressManager.start()


        getDataFromServer()

        if (isServiceRunning(DriverService::class.java, requireContext()))
        {
            Log.d("service", "already run")
            getRouteFromService()
        }

        binding.trackerButton.setOnClickListener {
            if (binding.routeList.selectedItemPosition == 0) // выбрана подсказка "маршрут не выбран"
                Snackbar.make(requireView(), getString(R.string.choose_route), Snackbar.LENGTH_LONG).show()
            else
                checkLocationPermissions()
        }
        return binding.root
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
                checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
            } else
            {
                if (isListiningPos) stopTracker()
                else startTracker()
                isListiningPos = !isListiningPos
            }
        else
        {
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getDataFromServer()
    {
        viewModel.viewModelScope.launch {
            val routeNames: Array<RouteNamesResponse>? = viewModel.getRouteNames()
            if (routeNames != null && routeNames.isNotEmpty())
            {
                Log.d("ResponseServer", "data (flight) Ready")

                val spinnerSet = Array<String>(routeNames.size) {
                    routeNames[it].name
                }

                val adapter = RouteArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, spinnerSet, getString(R.string.route_not_selected))
                adapter.setDropDownViewResource(R.layout.route_spinner_dropdown_item)
                binding.routeList.adapter = adapter
                val lastRouteId = lastRoutePreference.getRouteId()
                find@ for (i in routeNames.indices)
                    if (routeNames[i].id == lastRouteId)
                    {
                        binding.routeList.setSelection(i)
                        break@find
                    }

                progressManager.finish()
            }
            else
            {
                Log.d("ResponseServer", "data (flight) is null")
                // маршрутов нет ( 0 )
            }
        }
    }

    private fun getRouteFromService()
    {
        requireContext().startService(
            Intent(requireContext(), DriverService::class.java)
                .putExtra(getString(R.string.action), getString(R.string.which_tracker_id))
        )
    }

    private fun startTracker()
    {
        Log.d("Tracker", "start")
        val route = viewModel.routesInf[binding.routeList.selectedItemPosition]

        updateViews(route.name)

        requireContext().startService(
            Intent(requireContext(), DriverService::class.java)
                .putExtra(getString(R.string.route), Json.encodeToString(route))
                .putExtra(getString(R.string.action), getString(R.string.on_service))
        )
        lastRoutePreference.setRouteId(route.id)
        Snackbar.make(requireView(), getString(R.string.tracker_launched), Snackbar.LENGTH_LONG).show()
    }

    private fun stopTracker()
    {
        Log.d("Tracker", "stop")
        updateViews(null)

        requireContext().startService(
            Intent(requireContext(), DriverService::class.java)
                .putExtra(getString(R.string.action), getString(R.string.off_service))
        )
        Snackbar.make(requireView(), getString(R.string.tracker_shutdown), Snackbar.LENGTH_LONG).show()
    }

    private fun updateViews(routeName: String?)
    {
        if (routeName.isNullOrBlank())
        {
            binding.progressTracker.visibility = View.GONE
            binding.trackerButtonImage.setBackgroundResource(R.drawable.play_circle)
            binding.routeTrackingName.text = requireContext().getString(R.string.tracking_off)
        } else
        {
            binding.progressTracker.visibility = View.VISIBLE
            binding.trackerButtonImage.setBackgroundResource(R.drawable.stop_circle)
            binding.routeTrackingName.text = "${requireContext().getString(R.string.tracking_route)} $routeName"
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(serviceReceiver)
        super.onDestroy()
    }
}