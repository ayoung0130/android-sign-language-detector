package com.ayeong.sign_language_detector.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ayeong.sign_language_detector.R
import com.ayeong.sign_language_detector.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    private var backPressedTime: Long = 0 // 뒤로 가기 누른 시간 저장

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

        // 뒤로 가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }

        return binding.root
    }

    // 뒤로 가기 동작 처리
    private fun handleBackPress() {
        val currentTime = System.currentTimeMillis()

        // 첫 번째 뒤로 가기 클릭
        if (currentTime - backPressedTime > 2000) {
            backPressedTime = currentTime
            Toast.makeText(context, "뒤로 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show()
        } else {
            // 두 번째 뒤로 가기 클릭 -> 앱 종료
            requireActivity().finish()
        }
    }
}