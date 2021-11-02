package com.example.timetable.map

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.timetable.data.Repository
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptor

import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.timetable.R


class MapsFragment : Fragment() {

    lateinit var googleMap: GoogleMap

    private val callback = OnMapReadyCallback { google_map ->
        googleMap = google_map // ассинхронный вызов - в другом потоке
        Start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_maps, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun Start() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {
        /**
         * не пытайтесь разобраться в этом коде
         * тут я сам уже не понимаю что к чему...
         * */

        val data = Repository.busData
        var route = data?.route
        val polylineOptions = PolylineOptions()

            val startPoint = LatLng(route!![0].latitude, route!![0].longitude)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint,15f),1500, null)

        route.forEach { polylineOptions.add(LatLng( it.latitude,  it.longitude)) }
        val polyline = googleMap.addPolyline(polylineOptions)

//        var icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_icon)
        val circleDrawable = resources.getDrawable(com.example.timetable.R.drawable.bus_icon)
        val markerIcon: BitmapDescriptor = getMarkerIconFromDrawable(circleDrawable)

        data?.busStops?.forEach {
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng( it.position!!.latitude,  it.position!!.longitude))
                    .title(it.name)
                    .icon(markerIcon)

            )?.showInfoWindow()
        }

        Toast.makeText(context, data?.name, Toast.LENGTH_LONG).show()
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}