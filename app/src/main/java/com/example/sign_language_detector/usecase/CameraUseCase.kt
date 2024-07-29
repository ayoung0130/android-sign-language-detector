package com.example.sign_language_detector.usecase

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import com.example.sign_language_detector.ui.camera.CameraViewModel
import java.util.concurrent.ExecutorService

class CameraUseCase(
    private val fragment: Fragment,
    private val cameraFacing: Int,
    private val viewFinder: View,
    private val viewModel: CameraViewModel,
    private val backgroundExecutor: ExecutorService
) {

    @SuppressLint("UnsafeOptInUsageError")
    fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // 프리뷰 설정
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(viewFinder.display.rotation)
            .build()

        // 손 이미지 분석 설정
        val imageHandAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    viewModel.detectHand(image, cameraFacing == CameraSelector.LENS_FACING_FRONT)
                }
            }

        // 포즈 이미지 분석 설정
        val imagePoseAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    viewModel.detectPose(image, cameraFacing == CameraSelector.LENS_FACING_FRONT)
                }
            }

        // 사용 사례의 바인딩을 해제해야 함
        cameraProvider.unbindAll()

        try {
            // 카메라는 CameraControl 및 CameraInfo에 접근 제공
            val camera = cameraProvider.bindToLifecycle(
                fragment, cameraSelector, preview, imageHandAnalyzer, imagePoseAnalyzer
            )
            // 뷰 파인더의 서피스 공급자를 프리뷰 사용 사례에 연결
            preview.setSurfaceProvider((viewFinder as PreviewView).surfaceProvider)
        } catch (exc: Exception) {
            Log.e("CameraUseCaseBinder", "Use case binding failed", exc)
        }
    }
}
