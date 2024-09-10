package com.ayeong.sign_language_detector.ui.home

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    var navigateToQuestions: (() -> Unit)? = null
    var navigateToCamera: (() -> Unit)? = null

    fun onQuestionButtonClick() {
        navigateToQuestions?.invoke()
    }

    fun onTranslationButtonClick() {
        navigateToCamera?.invoke()
    }

}