package com.ayeong.sign_language_detector.ui.camera

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayeong.sign_language_detector.repository.HandLandmarkerHelper
import com.ayeong.sign_language_detector.repository.PoseLandmarkerHelper
import com.ayeong.sign_language_detector.usecase.DetectUseCase
import com.ayeong.sign_language_detector.util.LandmarkProcessor
import com.ayeong.sign_language_detector.util.ModelPredictProcessor
import com.ayeong.sign_language_detector.util.ProcessTts
import com.ayeong.sign_language_detector.util.WordsToSentence
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

    fun onSignLanguageVideoButtonClick() {
        navigateToSignLanguage?.invoke()
    }

    fun onBackButtonClick() {
        navigateBack?.invoke()
    }

    private val _predict = MutableLiveData<String?>()
    val predict: MutableLiveData<String?> get() = _predict

    private val _words = MutableLiveData<String?>()
    val words: MutableLiveData<String?> get() = _words

    // 램프 색상 상태 (true: 손/포즈 동시 검출, false: 미검출 상태)
    private val _isBothDetected = MutableLiveData<Boolean>()
    val isBothDetected: LiveData<Boolean> get() = _isBothDetected

    init {
        _isBothDetected.value = false // 초기 상태: 미검출 (false)
    }

    fun onBothDetected(isDetected: Boolean) {
        _isBothDetected.value = isDetected
    }

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

    fun modelPredict(data: List<FloatArray>): MutableList<String> {
        return modelPredictProcessor.predict(data)
    }

    fun processWords(words: MutableList<String>) {
        viewModelScope.launch {
            // words 리스트를 한 줄로 표시
            _words.postValue("모델 예측 결과: " + words.joinToString(" "))

            // 최종 결과를 받기 전까지는 _predict를 업데이트하지 않음
            val finalResult = processAndGenerateSentence(words)
            if (finalResult != null) {
                processTts.speak(finalResult)
            }
            _predict.postValue("GPT: $finalResult") // 최종 결과만 LiveData에 설정
        }
    }

    private suspend fun processAndGenerateSentence(words: MutableList<String>): String? {
        Log.d("tag", "단어 리스트: $words")
        val result = wordsToSentence.llm(words)
        return result?.trim()
    }
}
