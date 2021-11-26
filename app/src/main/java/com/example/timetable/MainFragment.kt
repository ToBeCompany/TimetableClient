package com.example.timetable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.timetable.data.TypeUser


class MainFragment : Fragment()
{

    val navController by lazy { this.requireView().findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_main, container, false)

        var typeUser = TypeUser.DRIVER


        when (typeUser)
        {
            TypeUser.DRIVER ->
            {
                navController.navigate(MainFragmentDirections.actionMainFragmentToDriverFragment())
            }
            TypeUser.COMMON ->
            {
                navController.navigate(MainFragmentDirections.actionMainFragmentToRouteFragment())
            }
            TypeUser.ADMIN -> {}
        }


        return root
    }

}