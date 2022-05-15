package com.dru128.timetable

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dru128.timetable.data.metadata.GeoPosition
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import dru128.timetable.R
import java.util.*

abstract class MapFragment: Fragment()
{
    lateinit var mapView: MapView
    lateinit var mapbox: MapboxMap
    val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val PERMISSION_CODE = 200

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        mapbox = mapView.getMapboxMap()
        mapbox.loadStyleUri(Style.MAPBOX_STREETS) {
            it.localizeLabels(getCurrentLocale(requireContext()))
            dataReady()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    abstract fun dataReady()


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            moveToUserLocation()
        else
            Snackbar.make(requireView(), getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    fun moveToUserLocation()
    {
        if (isGPSEnabled())
            if (isGPSPermissionGranted())
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
            else
            { // разрешения выданы

                mapView.location.apply {
                    if (!enabled)  updateSettings { enabled = true }
                    if (!pulsingEnabled) updateSettings { pulsingEnabled = true }
                }

                val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLocation != null)
                {
                    moveCamera(Point.fromLngLat(gpsLocation.longitude, gpsLocation.latitude))
                    Log.d("user_location", "GPS: ${gpsLocation.latitude}, ${gpsLocation.longitude}")
                }
                else
                {
                    val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (networkLocation != null)
                    {
                        moveCamera(Point.fromLngLat(networkLocation.longitude, networkLocation.latitude))
                        Log.d("user_location", "GPS: ${networkLocation.latitude}, ${networkLocation.longitude}")
                    }
                }
            }
        else
            Snackbar.make(requireView(), getString(R.string.turn_on_gps), Snackbar.LENGTH_LONG).show()
    }



    fun moveCamera(point: Point?) {
        if (point == null) return
        mapView.camera.easeTo(
            CameraOptions.Builder().center(point).zoom(14.0).build(),
            MapAnimationOptions.Builder().duration(3000).build()
        )
    }

    fun tpCamera(point: Point?) {
        if (point == null) return
        val cameraPosition = CameraOptions.Builder()
            .zoom(12.0)
            .center(point)
            .build()
        mapView.getMapboxMap().setCamera(cameraPosition)
    }

    fun geoPosToPoint(geoPosition: GeoPosition): Point =
        Point.fromLngLat(geoPosition.longitude, geoPosition.latitude)

    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    fun isGPSPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

    fun isGPSEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}