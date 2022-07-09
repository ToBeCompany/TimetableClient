package com.dru128.timetable.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentAdminMainBinding


class AdminMainFragment : Fragment()
{
    private lateinit var binding: FragmentAdminMainBinding
    private val viewModel: AdminMainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentAdminMainBinding.inflate(inflater)
        val navController = (childFragmentManager.findFragmentById(R.id.nav_host_admin) as NavHostFragment).navController


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapAdminFragment,
                R.id.editUsersFragment,
//                R.id.createRouteFragment
            )
        )
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.createRouteFragment) {
//                toolbar.visibility = View.GONE
                binding.bottomNavView.visibility = View.GONE
            } else {
//                toolbar.visibility = View.VISIBLE
                binding.bottomNavView.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

}