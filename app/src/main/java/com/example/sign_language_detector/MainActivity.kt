package com.example.sign_language_detector

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sign_language_detector.databinding.ActivityMainBinding
import com.example.sign_language_detector.fragment.PermissionsFragment
import com.google.mediapipe.framework.MediaPipeException
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), HandLandmarkerHelper.LandmarkerListener {

    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var handLandmarker: HandLandmarker
    private lateinit var backgroundExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // 백그라운드 실행기를 초기화
        backgroundExecutor = Executors.newSingleThreadExecutor()

        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("sign_language_detect_model.tflite")
                .setDelegate(Delegate.CPU)
                .build()
            val handLandmarkerOptions = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(2)
                .build()
            handLandmarker = HandLandmarker.createFromOptions(this, handLandmarkerOptions)
        } catch (e: MediaPipeException) {
            e.printStackTrace()
            Log.e("MainActivity", "MediaPipe HandLandmarker initialization failed", e)
        }

        try {
            handLandmarkerHelper = HandLandmarkerHelper(
                context = this,
                handLandmarkerHelperListener = this,
                runningMode = RunningMode.LIVE_STREAM
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MainActivity", "HandLandmarkerHelper initialization failed", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // 앱이 일시 중지된 상태에서 사용자가 권한을 제거할 수 있으므로, 모든 권한이 여전히 유효한지 확인
        if (!PermissionsFragment.hasPermissions(this)) {
            Navigation.findNavController(
                this, R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }

        // 사용자가 다시 전면으로 돌아올 때 HandLandmarkerHelper를 다시 시작
        backgroundExecutor.execute {
            if (handLandmarkerHelper.isClose()) {
                handLandmarkerHelper.setupHandLandmarker()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::handLandmarkerHelper.isInitialized) {
            viewModel.setMaxHands(handLandmarkerHelper.maxNumHands)
            viewModel.setMinHandDetectionConfidence(handLandmarkerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(handLandmarkerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(handLandmarkerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(handLandmarkerHelper.currentDelegate)

            // HandLandmarkerHelper를 닫고 자원을 해제
            backgroundExecutor.execute { handLandmarkerHelper.clearHandLandmarker() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 백그라운드 실행기를 종료
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
        val result = resultBundle.results.firstOrNull() ?: return

        val landmarks = result.landmarks()
        for (landmark in landmarks) {
            for (point in landmark) {
                val x = point.x()
                val y = point.y()
                val z = point.z()
                val visibility = point.visibility()
                Log.d("Landmark", "x: $x, y: $y, z: $z, visibility: $visibility")
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onError(error: String, errorCode: Int) {
        Log.e("MainActivity", "Error: $error, ErrorCode: $errorCode")
    }
}
