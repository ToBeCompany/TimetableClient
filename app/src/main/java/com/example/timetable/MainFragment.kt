package com.example.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.timetable.data.metadata.TypeUser


class MainFragment : Fragment()
{
    val userPreference by lazy { UserPreference(requireContext()) }
    lateinit var rootView: View



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_main, container, false)
        rootView = root
        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        checkUserAndNavigate()
    }

    private fun checkUserAndNavigate()
    {
        val navController = rootView.findNavController()

        if (userPreference.currentUser == null)
        {
            navController.navigate(MainFragmentDirections.actionMainFragmentToSignInFragment())

            // отправить на экран аунтификации
        }
        else
        {
            when (userPreference.currentUser!!.userType)
            {
                TypeUser.DRIVER -> {
                    navController.navigate(MainFragmentDirections.actionMainFragmentToDriverFragment())
                }
                TypeUser.WORKER -> {
                    navController.navigate(MainFragmentDirections.actionMainFragmentToRouteFragment())
                }
                TypeUser.ADMIN -> {}
                null -> {}
            }
        }
    }
}