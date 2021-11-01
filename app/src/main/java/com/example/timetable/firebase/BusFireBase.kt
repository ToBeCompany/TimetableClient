package com.example.timetable.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.timetable.data.BusData
import com.google.firebase.firestore.FirebaseFirestore


class BusFireBase()
{
    private val collection = "buses"
    var ref = FirebaseFirestore.getInstance().collection(collection)

    fun setBus(data: BusData)
    {
        ref
            .document()
            .set(data)
            .addOnFailureListener { e ->
                Log.d("error", e.message.toString())
            }
    }

//    fun getBuses(): MutableList<BusData>
//    {
//        db
//            .get()
//            .addOnSuccessListener { result ->
//                var data: MutableList<BusData> = mutableListOf()
//                for (doc in result)
//                    data.add(
//                        BusData(
//                            "6",
//                            mutableListOf<GeoPoint>(GeoPoint(3.3, 3.3), GeoPoint(3.3, 3.3)),
//                            mutableListOf(BusStop())
//                        )
//                    )
////            it.result?.documents
//        }
//
//    }
}