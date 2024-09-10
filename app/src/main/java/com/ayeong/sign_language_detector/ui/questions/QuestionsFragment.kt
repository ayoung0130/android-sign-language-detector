package com.ayeong.sign_language_detector.ui.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
            findNavController().navigateUp()
        }

        return binding.root
    }
}