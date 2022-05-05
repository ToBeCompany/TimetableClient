package com.dru128.timetable.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dru128.timetable.databinding.FragmentAdminMainBinding


class AdminMainFragment : Fragment()
{
    private lateinit var binding: FragmentAdminMainBinding
    private val viewModel: AdminMainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentAdminMainBinding.inflate(inflater)


        return binding.root
    }

}