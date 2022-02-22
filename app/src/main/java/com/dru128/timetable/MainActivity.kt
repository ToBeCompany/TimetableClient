package com.dru128.timetable

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.dru128.timetable.auth.SignInFragmentDirections
import com.dru128.timetable.auth.UserPreference
import com.dru128.timetable.data.metadata.TypeUser
import dru128.timetable.R
import dru128.timetable.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity()
{
    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.Theme_TimeTable)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkUserAndNavigate()
    }


    private fun checkUserAndNavigate()
    {
        val user = UserPreference(this).currentUser

        val inflater = navController.navInflater

        lifecycleScope.launch {
            if (user == null) {
                navController.graph = inflater.inflate(R.navigation.nav_auth)
                // отправить на экран аунтификации
            }
            else
            {

                if (viewModel.getUser(user.id) != null)
                {
                    when (user.userType) {
                        TypeUser.DRIVER -> {
                            navController.graph = inflater.inflate(R.navigation.nav_driver)
                        }
                        TypeUser.WORKER -> {
                            navController.graph = inflater.inflate(R.navigation.nav_worker)
                        }
                        TypeUser.ADMIN -> {}
                    }
                } else
                {
                    navController.graph = inflater.inflate(R.navigation.nav_auth)
                    navController.navigate(SignInFragmentDirections.actionSignInFragmentToNotFoundUserFragment())
                }
            }
            NavigationUI.setupActionBarWithNavController(this@MainActivity, navController)
        }
    }

    fun setActionBarTitle(title: String?) {
        supportActionBar!!.title = title
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
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
//                android.R.id.home -> onBackPressed() // эмуляция кнопки назад
        }
        return super.onOptionsItemSelected(item)
    }
}
