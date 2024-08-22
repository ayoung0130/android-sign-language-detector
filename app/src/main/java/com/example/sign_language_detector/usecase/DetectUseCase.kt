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
    fun detectHand(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        if (!executor.isShutdown && !executor.isTerminated) {
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
        } else {
            Log.e("DetectHandUseCase", "Executor is shut down, cannot execute task.")
        }
    }

    fun detectPose(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        if (!executor.isShutdown && !executor.isTerminated) {
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
        } else {
            Log.e("DetectPoseUseCase", "Executor is shut down, cannot execute task.")
        }
    }
}