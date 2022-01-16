package com.example.timetable.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import com.example.timetable.R
import com.example.timetable.UserPreference
import com.example.timetable.data.metadata.User
import com.google.android.material.snackbar.Snackbar


class SignInFragment : Fragment()
{
    private val viewModel: SignInViewModel by viewModels()
    private val userPreference by lazy { UserPreference(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var root = inflater.inflate(R.layout.fragment_sign_in, container, false)



        root.findViewById<EditText>(R.id.codetext_signInFragment)
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
                            root.findNavController().popBackStack()

                        }
                    }


                }
            }
        return root
    }

}