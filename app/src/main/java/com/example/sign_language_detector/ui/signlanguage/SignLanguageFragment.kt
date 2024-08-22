package com.example.sign_language_detector.ui.signlanguage

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sign_language_detector.R
import com.example.sign_language_detector.databinding.FragmentSignLanguageBinding

class SignLanguageFragment : Fragment() {

    private lateinit var binding: FragmentSignLanguageBinding
    private val viewModel: SignLanguageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignLanguageBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // ViewModel의 navigateBack 콜백 설정
        viewModel.navigateBack = {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        viewModel.loadSignLanguageItems(requireContext())
    }

    private fun setupRecyclerView() {
        binding.signLanguageRecyclerView.adapter = viewModel.adapter
    }
}
