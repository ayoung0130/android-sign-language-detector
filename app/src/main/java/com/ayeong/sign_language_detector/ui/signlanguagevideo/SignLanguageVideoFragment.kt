package com.ayeong.sign_language_detector.ui.signlanguagevideo

import android.net.Uri
import android.os.Bundle
import android.util.Log
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

        // 뒤로 가기 동작 설정
        setupBackNavigation()

        setupRecyclerView()
        observeSelectedVideo()
        setupVideoViewListeners()
        viewModel.loadSignLanguageItems(requireContext())

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.signLanguageRecyclerView.adapter = viewModel.adapter
    }

    private fun setupBackNavigation() {
        // navigateBack 콜백 설정
        viewModel.navigateBack = {
            navigateToCamera()
        }

        // 하드웨어 백 버튼 콜백 설정
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToCamera()
        }
    }

    private fun navigateToCamera() {
        runCatching {
            findNavController().navigate(R.id.action_sign_language_to_camera)
        }.onFailure { e ->
            Log.e("SignLanguageVideoFragment", "Navigation failed: ${e.message}")
        }
    }

    private fun observeSelectedVideo() {
        viewModel.selectedVideoUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                showVideo(uri)
            } else {
                hideVideo()
            }
        }
    }

    private fun setupVideoViewListeners() {
        // VideoView 재생 완료 리스너 설정 (자동으로 숨기지 않음)
        binding.signLanguageVideoView.setOnCompletionListener {
            // 재생이 완료되면 VideoView는 그대로 유지하고 사용자가 X 버튼이나 재생 버튼을 선택할 수 있도록 함
        }

        // X 버튼 클릭 시 VideoView 숨기기
        binding.closeButton.setOnClickListener {
            hideVideo()
        }

        // 재생 버튼 클릭 시 다시 재생
        binding.playButton.setOnClickListener {
            viewModel.selectedVideoUri.value?.let { uri -> playVideo(uri) }
        }
    }

    private fun showVideo(uri: Uri) {
        binding.videoContainer.visibility = View.VISIBLE
        playVideo(uri)
    }

    private fun playVideo(uri: Uri) {
        binding.signLanguageVideoView.setVideoURI(uri)
        binding.signLanguageVideoView.start()
    }

    private fun hideVideo() {
        binding.videoContainer.visibility = View.GONE
        binding.signLanguageVideoView.stopPlayback()
    }
}
