package com.example.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.timetable.map.MapViewModel
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity()
{
    private lateinit var navController: NavController
    private val m : MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.Theme_TimeTable)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
        lifecycleScope.launchWhenCreated {
            m.startWebSocket().collect {
                Log.d("sdefge", it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
    fun setActionBarTitle(title: String?) {
        supportActionBar!!.title = title
    }
}
