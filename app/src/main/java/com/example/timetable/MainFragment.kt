package com.example.timetable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import com.example.timetable.data.n.TypeUser
import com.example.timetable.data.n.User


class MainFragment : Fragment()
{
    val userPreference by lazy { UserPreference(requireContext()) }
    lateinit var rootView: View



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_main, container, false)
        rootView = root
        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        val navController = rootView.findNavController()

        if (userPreference.currentUser == null)
        {
            rootView.findViewById<TextView>(R.id.mainFragmentText)?.text = "user not auth"

            userPreference.authUserOnDevice(User(TypeUser.COMMON, "Andrei", "xcrypwui")) // for test
            // отправить на экран аунтификации
        }
        else
        {
            when (userPreference.currentUser!!.userType)
            {
                TypeUser.DRIVER -> {
                    navController.navigate(MainFragmentDirections.actionMainFragmentToDriverFragment())
                }
                TypeUser.COMMON -> {
                    navController.navigate(MainFragmentDirections.actionMainFragmentToRouteFragment())
                }
                TypeUser.ADMIN -> {}
                null -> {}
            }
        }
    }
}