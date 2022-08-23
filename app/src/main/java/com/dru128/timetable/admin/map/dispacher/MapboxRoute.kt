package com.dru128.timetable.admin.map.dispacher

import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation

class MapboxRoute(
    var isVisible: Boolean = false,
    var trackLine: PolylineAnnotation? = null,
    var busStops: List<PointAnnotation>? = null,
)