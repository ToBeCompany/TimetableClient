package com.example.timetable.data.n

import com.google.android.gms.maps.model.LatLng

data class Route(
    var name: String,
    var id: String,
    var points: MutableList<LatLng>,
    var busStops: MutableList<BusStop>
)
