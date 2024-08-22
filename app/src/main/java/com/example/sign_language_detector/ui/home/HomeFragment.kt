package com.example.sign_language_detector.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sign_language_detector.R
import com.example.sign_language_detector.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        with(viewModel){
            navigateToQuestions = {
                findNavController().navigate(R.id.action_home_to_questions)
            }
            navigateToCamera = {
                findNavController().navigate(R.id.action_home_to_camera)
            }
        }

        return binding.root
    }
}