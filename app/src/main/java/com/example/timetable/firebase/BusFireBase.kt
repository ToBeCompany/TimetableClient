package com.example.timetable.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.timetable.data.BusData
import com.google.firebase.firestore.FirebaseFirestore


class BusFireBase(var c: Context)
{
    private val collection = "buses"
    var ref = FirebaseFirestore.getInstance().collection(collection)

    fun setBus(data: BusData)
    {
        ref
            .document()
            .set(data)
            .addOnFailureListener { e ->
                Toast.makeText(c, e.message, Toast.LENGTH_LONG).show()
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