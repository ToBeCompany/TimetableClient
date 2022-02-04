package com.dru128.timetable.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import com.dru128.timetable.data.metadata.User
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentRouteBinding
import dru128.timetable.databinding.FragmentSignInBinding


class SignInFragment : Fragment()
{
    private lateinit var binding: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModels()
    private val userPreference by lazy { UserPreference(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = FragmentSignInBinding.inflate(inflater)
        binding
            .codeText
            .addTextChangedListener { inputText ->
                if (!inputText.isNullOrEmpty())
                {
                    lifecycle.coroutineScope.launchWhenStarted {

                        val response: User? = viewModel.getUser(inputText.toString())
                        Log.d("response", response.toString())

                        if (response != null && response.isNotEmpy())
                        {
                            userPreference.authUserOnDevice(response)
                            Snackbar.make(requireView(), getString(R.string.auth_success), Snackbar.LENGTH_LONG).show()
                            binding.root.findNavController().popBackStack()

                        }
                    }
                }
            }
        return binding.root
    }

}