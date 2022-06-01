package com.dru128.timetable.admin.map

import android.view.View
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation

class MapboxRoute(
    var isVisible: Boolean = false,
    var trackLine: PolylineAnnotation,
    var busStops: Array<PointAnnotation> = arrayOf(),
    var busStopTitles: Array<View> = arrayOf()
)