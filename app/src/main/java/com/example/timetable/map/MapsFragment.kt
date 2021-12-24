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
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.example.timetable.*
import com.example.timetable.data.Flight
import com.example.timetable.data.GeoPosition
import com.example.timetable.worker.BusStopsBottomSheet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.flow.collect


class MapsFragment : Fragment() {
    private var busMarker: Marker? = null

    private val args: MapsFragmentArgs by navArgs()

    private val viewModel: MapViewModel by viewModels()

    lateinit var googleMap: GoogleMap

    lateinit var flight: Flight
    private val callback = OnMapReadyCallback { google_map ->
        googleMap = google_map // ассинхронный вызов - в другом потоке

        lifecycle.coroutineScope.launchWhenStarted {
            flight = viewModel.getFlight(args.id)
            mapReady()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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



        startListeningTracker("1" /*Storage.flights[args.id].bus.id*/) //---------------

        val polylineOptions = PolylineOptions() // это будет маршрут (ломаная линия)

        (activity as MainActivity?)!!.setActionBarTitle(flight.name)
        moveCamera( flight.route.points[0] )// перемещаем камеру на первую остановку

        flight.route?.points?.forEach { polylineOptions.add(it) } // добавляем точки для линии
        val polyline = googleMap.addPolyline(polylineOptions) // добавляем линию (маршрут) на карту


        val markerIcon: BitmapDescriptor = getMarkerIconFromDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bus_icon,
                null
            )!! // создаем и конвертируем Drawable к BitmapDescriptor
        )

        val busStops = flight.route.busStops
        for (i in 0 until busStops.size) // добавляем маркеры на карту
        {
            var marker = googleMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            busStops[i].position.latitude,
                            busStops[i].position.longitude
                        )
                    )
                    .title(busStops[i].name)
                    .icon(markerIcon)
            )
                ?.setTag(i) // в тэг сохраняем индекс данных, потом по этому индексу будем находить даннные в массиве (ти-па привязки данных к маркеру)
        }



        Toast.makeText(context, flight.name, Toast.LENGTH_LONG).show()

        googleMap.setOnMarkerClickListener { marker -> // при нажатии на маркер
            if (marker.tag != null)
                BusStopsBottomSheet(marker.tag as Int, busStops)
                    .show(requireFragmentManager(), "BottomSheetDialog")
            true
        }


    }

    private fun startListeningTracker(id: String)
    {
        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.startWebSocket().collect {
                val busPosition = it.toLatLng()
//                Toast.makeText(App.globalContext, it + "  данные обновлены", Toast.LENGTH_LONG).show()
                if (busMarker == null)
                    busMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(busPosition)
                            .title("")
//                                    .icon(icon)
                    )
                else
                    busMarker?.position = busPosition
            }
        }



    }

    fun moveCamera(point: LatLng?) {
        if (point != null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f), 1500, null)
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

    private fun toLatLng(point: GeoPosition) = LatLng(point.latitude, point.longitude)
}