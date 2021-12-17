package com.example.timetable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.example.timetable.data.n.TypeUser
import com.example.timetable.data.n.User


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


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        val navController = rootView.findNavController()

        if (userPreference.currentUser == null)
        {
            rootView.findViewById<Button>(R.id.buttondriver).setOnClickListener {
                userPreference.authUserOnDevice(User(TypeUser.DRIVER, "Andrei", "xcrypwui")) // for test
                }
            rootView.findViewById<Button>(R.id.buttoncommon).setOnClickListener {
                userPreference.authUserOnDevice(User(TypeUser.WORKER, "Andrei", "xcrypwui")) // for test
                }
            rootView.findViewById<TextView>(R.id.mainFragmentText)?.text = "user not auth"

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