package com.dru128.timetable.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import com.dru128.timetable.MainActivity
import com.dru128.timetable.data.metadata.User
import com.dru128.timetable.tools.Keyboard
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentSignInBinding


class SignInFragment : Fragment()
{
    private lateinit var binding: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModels()
    private val userPreference by lazy { UserPreference(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentSignInBinding.inflate(inflater)

        Keyboard().show(requireContext(), binding.codeText)
        binding.signButton.setOnClickListener {
            val password = binding.codeText.text.toString()
                if (password.isNotBlank())
                {
                    lifecycle.coroutineScope.launchWhenStarted {
                        binding.invalidPasswordText.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                        val response: User? = viewModel.getUser(password)
                        binding.progressBar.visibility = View.GONE

                        Log.d("response", response.toString())

                        if (response != null && response.isNotEmpy())
                        {
                            Keyboard().hide(requireContext(), binding.codeText)
                            userPreference.authUserOnDevice(response)
                            Snackbar.make(requireView(), getString(R.string.auth_success), Snackbar.LENGTH_LONG).show()
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            (requireActivity() as AppCompatActivity).finish()
                        }
                        else
                        {
                            binding.invalidPasswordText.visibility = View.VISIBLE
                        }
                    }
                }
            }
        return binding.root

    }
}