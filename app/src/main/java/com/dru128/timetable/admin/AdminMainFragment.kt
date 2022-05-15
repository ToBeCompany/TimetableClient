package com.dru128.timetable.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dru128.timetable.R
import dru128.timetable.databinding.FragmentAdminMainBinding


class AdminMainFragment : Fragment()
{
    private lateinit var binding: FragmentAdminMainBinding
    private val viewModel: AdminMainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentAdminMainBinding.inflate(inflater)
//        var navController = binding.navHostAdmin.findFragment<NavHostFragment>().navController
        val navController = (childFragmentManager.findFragmentById(R.id.nav_host_admin) as NavHostFragment).navController


        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.mapAdminFragment,
            R.id.editUsersFragment))
        setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)
        return binding.root
    }

}