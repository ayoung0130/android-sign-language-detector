package com.example.sign_language_detector.repository

import android.content.Context
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class LandmarkerHelper(
    private var handLandmarkerHelper: HandLandmarkerHelper,
    var poseLandmarkerHelper: PoseLandmarkerHelper,
    val context: Context,
    private val landmarkerHelperListener: LandmarkerListener? = null
) {

    init {
        setupLandmarkers()
    }

    // 헬퍼 클래스 초기화
    private fun setupLandmarkers() {
        handLandmarkerHelper.setupHandLandmarker()
        poseLandmarkerHelper.setupPoseLandmarker()
    }

    // 라이브 스트림 감지 수행
    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        if (handLandmarkerHelper.runningMode != RunningMode.LIVE_STREAM || poseLandmarkerHelper.runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException("RunningMode.LIVE_STREAM이 아닌 상태에서 detectLiveStream을 호출하려고 합니다.")
        }

        // Hand 및 Pose 감지를 각각 수행
        handLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
        poseLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
    }

    // Hand 및 Pose 결과를 호출자에게 반환
    // todo - 반환한 결과 받아서 예측 수행하기
    private fun returnLivestreamResult(
        handResult: HandLandmarkerResult?,
        poseResult: PoseLandmarkerResult?,
        input: MPImage
    ) {
        landmarkerHelperListener?.onResults(
            CombinedResultBundle(
                handResult?.let { HandLandmarkerHelper.ResultBundle(listOf(it), input.height, input.width) },
                poseResult?.let { PoseLandmarkerHelper.ResultBundle(listOf(it), input.height, input.width) },
                input.height,
                input.width
            )
        )
    }

    data class CombinedResultBundle(
        val handResult: HandLandmarkerHelper.ResultBundle?,
        val poseResult: PoseLandmarkerHelper.ResultBundle?,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onResults(resultBundle: CombinedResultBundle)
    }
}
