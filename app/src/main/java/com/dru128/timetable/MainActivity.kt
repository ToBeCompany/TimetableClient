package com.dru128.timetable

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.dru128.timetable.auth.UserPreference
import dru128.timetable.R


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

        NavigationUI.setupActionBarWithNavController(this, navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
    fun setActionBarTitle(title: String?) {
        supportActionBar!!.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        if (UserPreference(this).currentUser != null)
            menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
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
