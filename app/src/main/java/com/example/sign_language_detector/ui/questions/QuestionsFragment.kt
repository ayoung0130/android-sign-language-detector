package com.example.sign_language_detector.ui.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sign_language_detector.R
import com.example.sign_language_detector.databinding.FragmentQuestionsBinding

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
            findNavController().navigate(R.id.action_questions_to_camera)
        }

        // 기기의 뒤로가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 여기에서 원하는 액션을 수행합니다.
                findNavController().navigate(R.id.action_questions_to_camera)
            }
        })

        return binding.root
    }
}