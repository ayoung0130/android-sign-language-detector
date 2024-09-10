package com.ayeong.sign_language_detector.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ayeong.sign_language_detector.usecase.DetectUseCase
import com.ayeong.sign_language_detector.util.LandmarkProcessor
import com.ayeong.sign_language_detector.util.ModelPredictProcessor
import com.ayeong.sign_language_detector.util.ProcessTts
import com.ayeong.sign_language_detector.util.WordsToSentence

class CameraViewModelFactory(
    private val detectUseCase: DetectUseCase,
    private val landmarkProcessor: LandmarkProcessor,
    private val modelPredictProcessor: ModelPredictProcessor,
    private val wordsToSentence: WordsToSentence,
    private val processTts: ProcessTts
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                detectUseCase,
                landmarkProcessor,
                modelPredictProcessor,
                wordsToSentence,
                processTts
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
