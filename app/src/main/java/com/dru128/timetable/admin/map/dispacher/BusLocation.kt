package com.dru128.timetable.admin.map.dispacher

import android.util.Log
import com.dru128.timetable.data.metadata.GeoPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BusLocation(
    var position: MutableStateFlow<GeoPosition?>  = MutableStateFlow(null),
    var busLocationJob: Job = Job()
) {
    private var scope: CoroutineScope = CoroutineScope(Job())
    var isActual = MutableStateFlow<Boolean>(false)

    private var countdownJob: Job? = null
    get()
    {
        Log.d("job", field?.isActive.toString())
        if (field == null  || !field!!.isActive)
            field = newCountdown()

        return field
    }

    init {
        scope.launch {
            position.collect {
                if (it != null)
                {
                    isActual.emit(true)
                    countdownJob!!.cancel()
                    countdownJob!!.start()
                }
            }
        }
    }

    fun newCountdown() = scope.launch (start = CoroutineStart.LAZY)
    {
        Log.d("event", "newCountdown")
        delay(60_000L)
        isActual.emit(false)
        Log.d("actual", "time 30 sec = false")
    }
}