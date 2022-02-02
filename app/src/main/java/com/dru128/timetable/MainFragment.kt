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


class MainFragment : Fragment()
{
    private val viewModel: MainViewModel by viewModels()
    lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_main, container, false)
        rootView = root

        return root
    }

    override fun onStart() {
        checkUserAndNavigate()
        super.onStart()
    }

    private fun checkUserAndNavigate()
    {
        val userPreference = UserPreference(requireContext())

        val navController = rootView.findNavController()

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