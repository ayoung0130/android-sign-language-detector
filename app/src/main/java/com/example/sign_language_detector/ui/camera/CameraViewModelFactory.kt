package com.example.sign_language_detector.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sign_language_detector.usecase.DetectUseCase

class CameraViewModelFactory(
    private val detectUseCase: DetectUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(detectUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
