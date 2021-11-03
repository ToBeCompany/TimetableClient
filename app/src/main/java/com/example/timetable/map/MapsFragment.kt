package com.example.timetable.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.timetable.R
import com.example.timetable.BottomSheet
import com.example.timetable.data.BusData
import com.example.timetable.data.BusStop
import com.example.timetable.data.Repository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class MapsFragment : Fragment() {

    lateinit var googleMap: GoogleMap
    private val args : MapsFragmentArgs by navArgs()

    private val callback = OnMapReadyCallback { google_map ->
        googleMap = google_map // ассинхронный вызов - в другом потоке
        Start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
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

        val data: BusData = Repository.busesData[args.id]
        var route = data.route
        val polylineOptions = PolylineOptions()

            val startPoint = LatLng(route!![0].latitude, route[0].longitude)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15f), 1500, null)

        route.forEach { polylineOptions.add(LatLng(it.latitude, it.longitude)) }
        val polyline = googleMap.addPolyline(polylineOptions)

//        var icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_icon)
        val circleDrawable = resources.getDrawable(com.example.timetable.R.drawable.bus_icon)
        val markerIcon: BitmapDescriptor = getMarkerIconFromDrawable(circleDrawable)

            val a = mutableMapOf<String, Int>()

        for (i in 0 until data.busStops!!.size)
        {
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(data.busStops[i].position!!.latitude, data.busStops[i].position!!.longitude))
                    .title(data.busStops[i].name)
                    .icon(markerIcon)

            )
        //?.showInfoWindow()
            a[marker!!.id] = i
        }



        Toast.makeText(context, data.name, Toast.LENGTH_LONG).show()

        googleMap.setOnMarkerClickListener { marker ->
            var bottomSheet = BottomSheet(args.id, a[marker.id]!!)
            a[marker.id]
            bottomSheet.show(requireFragmentManager(),"BottomSheetDialog")
            true
        }

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