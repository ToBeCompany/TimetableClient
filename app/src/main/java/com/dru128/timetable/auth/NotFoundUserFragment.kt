package com.dru128.timetable.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dru128.timetable.R


class NotFoundUserFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_not_found_user, container, false)

        root.findViewById<Button>(R.id.loginOut_NotFoundUserFragment)
            .setOnClickListener {
                val userPreference = UserPreference(requireContext())
                userPreference.loginOut()
                it.findNavController().popBackStack()
            }
        return root
    }


}