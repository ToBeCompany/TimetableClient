package com.dru128.timetable

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.dru128.timetable.auth.SignInFragmentDirections
import com.dru128.timetable.auth.UserPreference
import com.dru128.timetable.data.metadata.TypeUser
import com.dru128.timetable.data.metadata.User
import com.dru128.timetable.tools.ProgressManager
import dru128.timetable.R
import dru128.timetable.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity()
{
    private val viewModel: MainViewModel by viewModels()
    private lateinit var progressManager: ProgressManager

    private lateinit var binding: ActivityMainBinding
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.Theme_TimeTable)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressManager = ProgressManager(binding.parent, this@MainActivity)
        progressManager.start()

        checkUserAndNavigate()
    }


    private fun checkUserAndNavigate()
    {
        val user = UserPreference(this).currentUser

        val inflater = navController.navInflater

        lifecycleScope.launch {
            if (user == null) {
                this@MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                navController.graph = inflater.inflate(R.navigation.nav_auth)
                // отправить на экран аунтификации
            }
            else
            {
                supportActionBar?.hide()
                if (viewModel.getUser(user.id) != null)
                {
                    supportActionBar?.show()
                    when (user.userType) {
                        TypeUser.DRIVER -> {
                            this@MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            navController.graph = inflater.inflate(R.navigation.nav_driver)
                        }
                        TypeUser.WORKER -> {
                            this@MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            navController.graph = inflater.inflate(R.navigation.nav_worker)
                        }
                        TypeUser.ADMIN -> {
                            this@MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
//                            this@MainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            navController.graph = inflater.inflate(R.navigation.nav_admin)
                        }
                    }
                } else
                {
                    navController.graph = inflater.inflate(R.navigation.nav_auth)
                    navController.navigate(SignInFragmentDirections.actionSignInFragmentToNotFoundUserFragment())
                }
            }
            progressManager.finish()
            NavigationUI.setupActionBarWithNavController(this@MainActivity, navController)
        }
    }

    fun setActionBarTitle(newTitle: String) {
        supportActionBar?.title = newTitle
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        if (UserPreference(this).currentUser != null)
            menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.logout ->
            {
                val userPreference = UserPreference(this)
                userPreference.loginOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
