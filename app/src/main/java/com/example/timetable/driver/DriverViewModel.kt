package com.example.timetable.driver

import android.annotation.SuppressLint
import android.app.Application
import android.app.SearchManager
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timetable.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.os.Bundle
import android.widget.Toast

import androidx.annotation.NonNull
import com.example.timetable.App
import com.google.firebase.firestore.FirebaseFirestore


class DriverViewModel(application : Application): AndroidViewModel(application) {
    private val locationManager by lazy {
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var ref = FirebaseFirestore.getInstance().collection("LOCATION").document("point")

    private val _tapRequestState: MutableStateFlow<Resource<Location>?> = MutableStateFlow(null)
    val tapRequestState: StateFlow<Resource<Location>?> = _tapRequestState

    private val listener = LocationListener { // тут данные меняются
        Toast.makeText(App.globalContext, it.toString() +";;;", Toast.LENGTH_SHORT).show()

        var map = hashMapOf(
            "lat" to it.latitude,
            "lon" to it.longitude
        )
        ref.set(map)
        _tapRequestState.value = Resource.Success(it)

    }

    @SuppressLint("MissingPermission")
    fun startSearch() {
        _tapRequestState.value = Resource.Loading()

        val lastPositionNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5_000,
                10f,
                listener
            )

    }
    fun clearSearch() {
        _tapRequestState.value = null
        locationManager.removeUpdates(listener)
    }
}