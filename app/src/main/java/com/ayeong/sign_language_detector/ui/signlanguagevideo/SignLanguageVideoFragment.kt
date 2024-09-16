package com.ayeong.sign_language_detector.ui.signlanguagevideo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ayeong.sign_language_detector.R
import com.ayeong.sign_language_detector.databinding.FragmentSignLanguageVideoBinding

class SignLanguageVideoFragment : Fragment() {

    private lateinit var binding: FragmentSignLanguageVideoBinding
    private val viewModel: SignLanguageVideoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignLanguageVideoBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // ViewModel의 navigateBack 콜백 설정
        viewModel.navigateBack = {
            findNavController().navigate(R.id.action_sign_language_to_camera)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_sign_language_to_camera)
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
