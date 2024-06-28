package com.example.sign_language_detector.ui.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sign_language_detector.databinding.FragmentQuestionsBinding

class QuestionsFragment : Fragment() {

    private var _fragmentQuestionsBinding: FragmentQuestionsBinding? = null
    private val fragmentQuestionsBinding
        get() = _fragmentQuestionsBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentQuestionsBinding =
            FragmentQuestionsBinding.inflate(inflater, container, false)

        return fragmentQuestionsBinding.root
    }

}