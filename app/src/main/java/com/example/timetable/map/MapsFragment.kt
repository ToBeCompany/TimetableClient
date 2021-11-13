package com.example.timetable.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.timetable.R
import com.example.timetable.BottomSheet
import com.example.timetable.data.BusData
import com.example.timetable.data.Repository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint


class MapsFragment : Fragment() {

    lateinit var googleMap: GoogleMap
    private val args : MapsFragmentArgs by navArgs()

    private val callback = OnMapReadyCallback { google_map ->
        googleMap = google_map // ассинхронный вызов - в другом потоке
        mapReady()
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

    private fun mapReady() // это вызывается когда данные карт получены и можно работать (аналог onCreate)
    {

        val data: BusData = Repository.busesData[args.id]
        val polylineOptions = PolylineOptions() // это будет маршрут (ломаная линия)

        moveCamera( toLatLng(data.route?.get(0)) ) // перемещаем камеру на первую остановку

        data.route?.forEach { polylineOptions.add( toLatLng(it) ) } // добавляем точки для линии
        val polyline = googleMap.addPolyline(polylineOptions) // добавляем линию (маршрут) на карту


        val markerIcon: BitmapDescriptor = getMarkerIconFromDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.bus_icon, null)!! // создаем и конвертируем Drawable к BitmapDescriptor
        )

        for (i in 0 until data.busStops!!.size) // добавляем маркеры на карту
        {
            var marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(data.busStops[i].position!!.latitude, data.busStops[i].position!!.longitude))
                    .title(data.busStops[i].name)
                    .icon(markerIcon)
            )?.setTag(i) // в тэг сохраняем индекс данных, потом по этому индексу будем находить даннные в массиве (ти-па привязки данных к маркеру)
        }



        Toast.makeText(context, data.name, Toast.LENGTH_LONG).show()

        googleMap.setOnMarkerClickListener { marker -> // при нажатии на маркер
            BottomSheet(marker.tag as Int, data.busStops)
                .show(requireFragmentManager(),"BottomSheetDialog")
            true
        }

    }

    fun moveCamera(point: LatLng?)
    {
        if (point != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f), 1500, null)
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor
    {
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

    private fun toGeoPoint(point: LatLng?) = point?.let { GeoPoint(it.latitude, point.longitude) }
    private fun toLatLng(point: GeoPoint?) = point?.let { LatLng(it.latitude, point.longitude) }
}