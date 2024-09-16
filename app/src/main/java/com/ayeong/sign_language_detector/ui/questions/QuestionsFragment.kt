package com.ayeong.sign_language_detector.ui.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ayeong.sign_language_detector.R
import com.ayeong.sign_language_detector.databinding.FragmentQuestionsBinding

class QuestionsFragment : Fragment() {

    private lateinit var viewModel: QuestionsViewModel
    private lateinit var binding: FragmentQuestionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[QuestionsViewModel::class.java]

        binding = FragmentQuestionsBinding.inflate(inflater, container, false).apply {
            viewModel = this@QuestionsFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        // ViewModel의 navigateBack 콜백 설정
        viewModel.navigateBack = {
            handleBackNavigation()
        }

        // 뒤로 가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackNavigation()
        }

        return binding.root
    }

    private fun handleBackNavigation() {
        // 현재 네비게이션 스택 상태를 파악
        val navController = findNavController()

        // 이전 백스택 엔트리가 카메라 프래그먼트인지 확인
        val previousBackStackEntry = navController.previousBackStackEntry
        if (previousBackStackEntry?.destination?.id == R.id.camera_fragment) {
            // 홈 -> 카메라 -> 질문일 때, 카메라 화면으로 돌아감
            navController.navigate(R.id.camera_fragment)
        } else {
            // 그 외에는 홈으로 돌아감
            navController.navigate(R.id.home_fragment)
        }
    }
}