package com.example.sign_language_detector.ui.camera

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.example.sign_language_detector.usecase.DetectUseCase

class CameraViewModel(
    private val detectUseCase: DetectUseCase
) : ViewModel() {

    fun detectHand(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        detectUseCase.detectHand(imageProxy, isFrontCamera)
    }

    fun detectPose(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        detectUseCase.detectPose(imageProxy, isFrontCamera)
    }
}