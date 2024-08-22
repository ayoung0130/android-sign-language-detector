package com.example.sign_language_detector.ui.camera

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import com.example.sign_language_detector.usecase.DetectUseCase
import com.example.sign_language_detector.util.LandmarkProcessor
import com.example.sign_language_detector.util.ModelPredictProcessor
import com.example.sign_language_detector.util.ProcessTts
import com.example.sign_language_detector.util.WordsToSentence
import kotlinx.coroutines.launch

class CameraViewModel(
    private val detectUseCase: DetectUseCase,
    private val landmarkProcessor: LandmarkProcessor,
    private val modelPredictProcessor: ModelPredictProcessor,
    private val wordsToSentence: WordsToSentence,
    private val processTts: ProcessTts
) : ViewModel() {

    var navigateToHome: (() -> Unit)? = null
    var navigateToQuestions: (() -> Unit)? = null
    var navigateToSignLanguage: (() -> Unit)? = null
    var navigateBack: (() -> Unit)? = null

    fun onHomeButtonClick() {
        navigateToHome?.invoke()
    }

    fun onQuestionButtonClick() {
        navigateToQuestions?.invoke()
    }

    fun onSignLanguageButtonClick() {
        navigateToSignLanguage?.invoke()
    }

    fun onBackButtonClick() {
        navigateBack?.invoke()
    }

    private val _predict = MutableLiveData<String?>()
    val predict: MutableLiveData<String?> get() = _predict

    fun detectHand(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        detectUseCase.detectHand(imageProxy, isFrontCamera)
    }

    fun detectPose(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        detectUseCase.detectPose(imageProxy, isFrontCamera)
    }

    fun processLandmarks(
        handResultBundle: HandLandmarkerHelper.ResultBundle,
        poseResultBundle: PoseLandmarkerHelper.ResultBundle
    ): FloatArray {
        return landmarkProcessor.processLandmarks(handResultBundle, poseResultBundle)
    }

    fun modelPredict(data: List<FloatArray>, context: Context): MutableList<Int> {
        return modelPredictProcessor.predict(data, context)
    }

    fun processWords(words: MutableList<Int>) {
        viewModelScope.launch {
            // 최종 결과를 받기 전까지는 _predict를 업데이트하지 않음
            val finalResult = processAndGenerateSentence(words)
            if (finalResult != null) {
                processTts.speak(finalResult)
            }
            _predict.postValue(finalResult) // 최종 결과만 LiveData에 설정
        }
    }

    private suspend fun processAndGenerateSentence(words: MutableList<Int>): String? {
        val result = wordsToSentence.llm(words)
        return result?.trim()
    }
}
