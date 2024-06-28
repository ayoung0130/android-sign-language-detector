package com.example.sign_language_detector.ui.questions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuestionsViewModel : ViewModel() {

    private val _symptomQuestions = MutableLiveData<List<String>>()
    val symptomQuestions: LiveData<List<String>> get() = _symptomQuestions

    private val _diagnosisQuestions = MutableLiveData<List<String>>()
    val diagnosisQuestions: LiveData<List<String>> get() = _diagnosisQuestions

    var navigateBack: (() -> Unit)? = null

    init {
        // Sample questions
        _symptomQuestions.value = listOf(
            "어디가 아파서 오셨어요?",
            "증상이 언제부터 시작됐나요?",
            "다른 증상은 없으신가요?"
        )
        _diagnosisQuestions.value = listOf(
            "현재 어떤 약을 복용 중이신가요?",
            "과거 병력이 있으신가요?",
            "질문 추가 예정"
        )
    }

    fun onBackButtonClick() {
        navigateBack?.invoke()
    }
}
