package com.example.timetable

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI


class MainActivity : AppCompatActivity()
{
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.Theme_TimeTable)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate")

        NavigationUI.setupActionBarWithNavController(this, navController)
    }

//    override fun onNewIntent(intent: Intent?) {
//        var id = intent?.getStringExtra(getString(R.string.tracker_id))
//        Log.d("MainActivity", "onNewIntent, routeId = $id")
//        super.onNewIntent(intent)
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
    fun setActionBarTitle(title: String?) {
        supportActionBar!!.title = title
    }
}
