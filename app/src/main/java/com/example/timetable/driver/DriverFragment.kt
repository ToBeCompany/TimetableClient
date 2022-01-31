package com.example.timetable.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.example.timetable.R
import com.example.timetable.data.metadata.response.FlightsNameResponse
import com.example.timetable.driver.service.DriverService
import com.example.timetable.system.ProgressManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.example.timetable.system.isServiceRunning
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.IntentFilter
import android.content.BroadcastReceiver
import kotlinx.serialization.decodeFromString


class DriverFragment : Fragment()
{
    private val viewModel: DriverViewModel by viewModels()
    private val PERMISSION_CODE = 200

    private lateinit var lastRoutePreference: LastRoutePreference
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private lateinit var progressManager: ProgressManager

    private lateinit var trackerButton: ImageButton
    private lateinit var curRouteText: TextView
    private lateinit var routeSpinner: Spinner

    private var isListiningPos = false

    var serviceReceiver: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            if (intent.hasExtra(getString(R.string.tracker_id)))
            {
                val jsonRoute = intent.getStringExtra(getString(R.string.tracker_id)).toString()
                Log.d("receiver_driver", jsonRoute)

                val route: FlightsNameResponse = Json.decodeFromString(jsonRoute)

                isListiningPos = true
                trackerButton.setBackgroundResource(R.drawable.stop_circle)
                curRouteText.text = "${requireContext().getString(R.string.tracking_route)} ${route.name}"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_driver, container, false)

        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(serviceReceiver, IntentFilter(getString(R.string.which_tracker_id)))

        lastRoutePreference = LastRoutePreference(requireContext())

        progressManager = ProgressManager(root.findViewById(R.id.parentDriverFragment), requireActivity())
        progressManager.start()

        trackerButton = root.findViewById(R.id.ButtonTracker_fragmentDriver)
        curRouteText = root.findViewById(R.id.routeTrackingText_DriverFragment)
        routeSpinner = root.findViewById<Spinner>(R.id.routeList_DriverFragment)

        getDataFromServer()

        if (isServiceRunning(DriverService::class.java, requireContext()))
        {
            Log.d("service", "already run")
            getRouteFromService()
        }

        trackerButton.setOnClickListener {
            if (routeSpinner.selectedItemPosition == 0) // выбрана подсказка "маршрут не выбран"
                Snackbar.make(requireView(), getString(R.string.choose_route), Snackbar.LENGTH_LONG).show()
            else
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
            val routeNames: Array<FlightsNameResponse>? = viewModel.getFlight()
            if (routeNames != null && routeNames.isNotEmpty())
            {
                Log.d("ResponseServer", "data (flight) Ready")
                progressManager.finish()

                var spinnerSet = arrayOf<String>()
                spinnerSet += routeNames.map { it.name.toString() }

                val adapter = RouteArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, spinnerSet, getString(R.string.route_not_selected))
                adapter.setDropDownViewResource(R.layout.route_spinner_dropdown_item)
                routeSpinner.adapter = adapter
                val lastRouteId = lastRoutePreference.getRouteId()
                find@ for (i in routeNames.indices)
                    if (routeNames[i].id == lastRouteId)
                    {
                        routeSpinner.setSelection(i)
                        break@find
                    }
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
        val route = viewModel.routesInf[routeSpinner.selectedItemPosition]
        if (route.id != null && route.name != null)
        {
            trackerButton.setBackgroundResource(R.drawable.stop_circle)
            curRouteText.text = "${requireContext().getString(R.string.tracking_route)} ${route.name} ${routeSpinner.selectedItemPosition}"
            requireContext().startService(
                Intent(requireContext(), DriverService::class.java)
                    .putExtra(getString(R.string.route), Json.encodeToString(route))
                    .putExtra(getString(R.string.action), getString(R.string.on_service))
            )
            lastRoutePreference.setRouteId(route.id!!)
            Snackbar.make(requireView(), getString(R.string.tracker_launched), Snackbar.LENGTH_LONG).show()

        }
    }

    private fun stopTracker()
    {
        Log.d("Tracker", "stop")
        trackerButton.setBackgroundResource(R.drawable.play_circle)

        curRouteText.text = requireContext().getString(R.string.tracking_off)
        requireContext().stopService(Intent(requireContext(), DriverService::class.java))
        Snackbar.make(requireView(), getString(R.string.tracker_shutdown), Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(serviceReceiver)
    }
}