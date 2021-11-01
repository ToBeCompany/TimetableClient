package com.example.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity()
{

    val t by lazy { findViewById<TextView>(R.id.tt) }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = Firebase.firestore

        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

        db.collection("USer").add(user)
            .addOnSuccessListener { documentReference ->
                t.text = "success"
            }
            .addOnFailureListener { e ->
                t.text =  e.message.toString()
                Log.d("Error", e.message.toString())

            }
    }
}