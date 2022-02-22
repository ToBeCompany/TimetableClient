package com.dru128.timetable.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import com.dru128.timetable.MainActivity
import com.dru128.timetable.data.metadata.User
import com.google.android.material.snackbar.Snackbar
import dru128.timetable.R
import dru128.timetable.databinding.FragmentSignInBinding


class SignInFragment : Fragment()
{
    private lateinit var binding: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModels()
    private val userPreference by lazy { UserPreference(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            ActivityCompat.finishAffinity(requireActivity())
        }
        binding = FragmentSignInBinding.inflate(inflater)

        showSoftKeyboard(binding.codeText)
        binding
            .codeText
            .addTextChangedListener { code ->
                if (!code.isNullOrEmpty())
                {
                    lifecycle.coroutineScope.launchWhenStarted {

                        val response: User? = viewModel.getUser(code.toString())
                        Log.d("response", response.toString())

                        if (response != null && response.isNotEmpy())
                        {
                            userPreference.authUserOnDevice(response)
                            Snackbar.make(requireView(), getString(R.string.auth_success), Snackbar.LENGTH_LONG).show()
                            binding.root.findNavController().popBackStack()
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            (requireActivity() as AppCompatActivity).finish()
                        }
                    }
                }
            }
        return binding.root

    }
    fun showSoftKeyboard(editText: EditText)
    {
        editText.requestFocus()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
            ?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        super.onDestroy()
    }
}