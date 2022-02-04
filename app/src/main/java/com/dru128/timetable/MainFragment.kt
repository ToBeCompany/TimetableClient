package com.dru128.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import com.dru128.timetable.auth.UserPreference
import com.dru128.timetable.data.metadata.TypeUser
import dru128.timetable.R
import dru128.timetable.databinding.FragmentMainBinding
import dru128.timetable.databinding.FragmentSignInBinding


class MainFragment : Fragment()
{
    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentMainBinding.inflate(inflater)

        return binding.root
    }

    override fun onStart() {
        checkUserAndNavigate()
        super.onStart()
    }

    private fun checkUserAndNavigate()
    {
        val userPreference = UserPreference(requireContext())

        val navController = binding.root.findNavController()

        if (userPreference.currentUser == null)
        {
            navController.navigate(MainFragmentDirections.actionMainFragmentToSignInFragment())
            // отправить на экран аунтификации
        }
        else
        {
            lifecycle.coroutineScope.launchWhenStarted {
                if (viewModel.getUser(userPreference.currentUser!!.id) != null)
                    when (userPreference.currentUser!!.userType)
                    {
                        TypeUser.DRIVER -> {
                            navController.navigate(MainFragmentDirections.actionMainFragmentToDriverFragment())
                        }
                        TypeUser.WORKER -> {
                            navController.navigate(MainFragmentDirections.actionMainFragmentToRouteFragment())
                        }
                        TypeUser.ADMIN -> {}
                    }
                else
                    navController.navigate(MainFragmentDirections.actionMainFragmentToNotFoundUserFragment())
            }
        }
    }
}