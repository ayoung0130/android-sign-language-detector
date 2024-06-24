package com.example.sign_language_detector.usecase

import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import com.example.sign_language_detector.util.LandmarkProcessor
import java.util.concurrent.ExecutorService

class DetectUseCase(
    private val handLandmarkerHelper: HandLandmarkerHelper,
    private val poseLandmarkerHelper: PoseLandmarkerHelper,
    private val executor: ExecutorService
) {

    private val landmarkProcessor = LandmarkProcessor()

    fun detectHand(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        executor.execute {
            try {
                handLandmarkerHelper.detectLiveStream(
                    imageProxy = imageProxy,
                    isFrontCamera = isFrontCamera
                )
            } catch (e: Exception) {
                Log.e("DetectHandUseCase", "Hand detection failed", e)
            }
        }
    }

    fun detectPose(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        executor.execute {
            try {
                poseLandmarkerHelper.detectLiveStream(
                    imageProxy = imageProxy,
                    isFrontCamera = isFrontCamera
                )
            } catch (e: Exception) {
                Log.e("DetectPoseUseCase", "Pose detection failed", e)
            }
        }
    }

    fun processLandmarks(resultHandBundle: HandLandmarkerHelper.ResultBundle, resultPoseBundle: PoseLandmarkerHelper.ResultBundle) {
        executor.execute {
            landmarkProcessor.processLandmarks(resultHandBundle, resultPoseBundle)
            Log.d("결과", "$resultHandBundle")
            Log.d("결과", "$resultPoseBundle")
        }
    }
}