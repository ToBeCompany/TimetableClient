package com.dru128.timetable.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dru128.timetable.MainActivity
import dru128.timetable.R


class NotFoundUserFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_not_found_user, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            ActivityCompat.finishAffinity(requireActivity())
        }
        root.findViewById<Button>(R.id.login_out)
            .setOnClickListener {
                val userPreference = UserPreference(requireContext())
                userPreference.loginOut()
                restartApp()
            }
        root.findViewById<Button>(R.id.reboot)
            .setOnClickListener {
                restartApp()
            }
        return root
    }

    private fun restartApp()
    {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        (requireActivity() as AppCompatActivity).finish()
    }


}