package com.example.sign_language_detector.ui.camera

import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import com.example.sign_language_detector.usecase.DetectUseCase
import com.example.sign_language_detector.util.LandmarkProcessor
import com.example.sign_language_detector.util.ModelPredictProcessor

class CameraViewModel(
    private val detectUseCase: DetectUseCase,
    private val landmarkProcessor: LandmarkProcessor,
    private val modelPredictProcessor: ModelPredictProcessor
) : ViewModel() {

    private val _predictedWord = MutableLiveData<String>()
    val predictedWord: LiveData<String> get() = _predictedWord

    fun detectHand(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        detectUseCase.detectHand(imageProxy, isFrontCamera)
    }

    fun detectPose(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        detectUseCase.detectPose(imageProxy, isFrontCamera)
    }

    fun processLandmarks(
        resultHandBundles: HandLandmarkerHelper.ResultBundle,
        resultPoseBundles: PoseLandmarkerHelper.ResultBundle
    ) {
        landmarkProcessor.processLandmarks(resultHandBundles, resultPoseBundles)
        val data = landmarkProcessor.getLandmarkData()
        val prediction = modelPredictProcessor.predict(data)
        updatePredictedWord(prediction)
    }

    private fun updatePredictedWord(word: String) {
        _predictedWord.postValue(word)
    }
}
