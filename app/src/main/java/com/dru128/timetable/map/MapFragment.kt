package com.dru128.timetable.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.dru128.timetable.App
import com.dru128.timetable.data.metadata.BusStop
import com.dru128.timetable.data.metadata.GeoPosition
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import dru128.timetable.R
import java.io.StringReader
import java.util.*


abstract class MapFragment: Fragment()
{
    private object MapFragmentState
    {
        var cameraZoom: Double? = null
        var cameraPosition: Point? = null
    }

    lateinit var mapView: MapView
    lateinit var mapbox: MapboxMap

    val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val PERMISSION_CODE = 200

    lateinit var pointAnnotationManager: PointAnnotationManager
    lateinit var polylineAnnotationManager: PolylineAnnotationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        mapbox = mapView.getMapboxMap()
        mapbox.setBounds(agglomerationOfBarnaul)
        mapbox.loadStyleUri(Style.MAPBOX_STREETS) {
            it.localizeLabels(getCurrentLocale(requireContext()))
            mapReady()
        }


        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
        polylineAnnotationManager = mapView.annotations.createPolylineAnnotationManager()

        Log.d("MAP_CAMERA", "position: " + MapFragmentState.cameraPosition.toString())
        Log.d("MAP_CAMERA", "zoom: " + MapFragmentState.cameraZoom.toString())

        if (MapFragmentState.cameraZoom != null && MapFragmentState.cameraPosition != null)
            tpCamera(MapFragmentState.cameraPosition, MapFragmentState.cameraZoom)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    abstract fun mapReady()


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            moveToUserLocation()
        else
            Snackbar.make(requireView(), getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun moveToUserLocation()
    {
        if (isGPSEnabled())
            if (isGPSPermissionGranted())
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
            else
            { // разрешения выданы и gps включен

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




    fun createBusStop(busStopData: BusStop, busStopIcon: Bitmap): PointAnnotationOptions
    {
        return PointAnnotationOptions()
            .withPoint(geoPosToPoint(busStopData.position))
            .withData(
                    JsonParser.parseReader(StringReader(busStopData.id))
            )
            .withIconImage(busStopIcon)
            .withIconAnchor(IconAnchor.BOTTOM)
            .withTextField(busStopData.name)
            .withTextAnchor(TextAnchor.TOP)
            .withTextSize(10.0)
    }
    fun createRouteLine(points: List<Point>): PolylineAnnotationOptions
    {
        return PolylineAnnotationOptions()
            .withPoints(points)
            .withLineColor( ResourcesCompat.getColor(requireContext().resources, R.color.polyline, null) )
            .withLineWidth(6.0)
            .withLineJoin(LineJoin.ROUND)
    }

    fun moveCamera(point: GeoPosition?) {
        if (point != null)
            moveCamera(geoPosToPoint(point))
    }
    fun moveCamera(point: Point?) {
        if (point == null) return
        mapView.camera.easeTo(
            CameraOptions
                .Builder()
                .center(point)
                .zoom(14.0)
                .build(),
            MapAnimationOptions
                .Builder()
                .duration(3000)
                .build()
        )
    }

    fun tpCamera(point: GeoPosition?, zoom: Double? = null) {
        if (point != null)
            tpCamera(geoPosToPoint(point), zoom)
    }
    fun tpCamera(point: Point?, zoom: Double? = null) {
        if (point == null) return
        val cameraPosition = CameraOptions.Builder()
            .zoom(zoom ?: 12.0)
            .center(point)
            .build()

        mapbox.setCamera(cameraPosition)
    }

    fun geoPosToPoint(geoPosition: GeoPosition): Point =
        Point.fromLngLat(geoPosition.longitude, geoPosition.latitude)

    fun geoPosToPoint(geoPositions: List<GeoPosition>): List<Point> =
        List<Point>(geoPositions.size) { i -> geoPosToPoint(geoPositions[i]) }

    fun pointToGeoPos(point: Point): GeoPosition =
        GeoPosition(latitude = point.latitude(), longitude = point.longitude())


    private fun getCurrentLocale(context: Context): Locale
    {
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

    private val agglomerationOfBarnaul: CameraBoundsOptions = CameraBoundsOptions.Builder()
        .bounds(
            CoordinateBounds(
                Point.fromLngLat(82.113161,52.79682),
                Point.fromLngLat(85.612297,53.967066),
                false
            )
        )
        .minZoom(8.0)
        .build()

    override fun onStop()
    {
        MapFragmentState.cameraZoom = mapbox.cameraState.zoom
        MapFragmentState.cameraPosition = mapbox.cameraState.center
        super.onStop()
    }
}