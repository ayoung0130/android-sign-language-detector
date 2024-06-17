package com.example.sign_language_detector

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sign_language_detector.databinding.ActivityMainBinding
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()

    private lateinit var tflite: Interpreter
    private lateinit var handLandmarker: HandLandmarker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }

        // Initialize TFLite Interpreter
        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Initialize MediaPipe HandLandmarker
        val handLandmarkerOptions = HandLandmarker.HandLandmarkerOptions.builder()
            .setNumHands(1)
            .build()
        handLandmarker = HandLandmarker.createFromOptions(this, handLandmarkerOptions)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = this.assets.openFd("sign_language_detect_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocessData(bitmap: Bitmap, numFrames: Int): Array<FloatArray> {
        val options = ImageProcessingOptions.builder().build()
        val results = mutableListOf<HandLandmarkerResult>()

        // 여기서 numFrames 동안의 데이터를 수집합니다.
        for (i in 0 until numFrames) {
            val mpImage = convertBitmapToMPImage(bitmap)
            val result = handLandmarker.detect(mpImage, options)
            if (result.isNotEmpty()) {
                results.add(result[0]) // 단일 프레임 결과만 가져옵니다.
            }
        }

        val numLandmarks = results[0].landmarks().size
        val numCoordinates = 3 // x, y, z 좌표
        val totalCoordinates = numLandmarks * numCoordinates

        // 2차원 배열 생성: [프레임 수][각 관절당 좌표값]
        val data = Array(numFrames) {
            FloatArray(totalCoordinates)
        }

        for (i in results.indices) {
            val landmarks = results[i].landmarks()
            for (j in landmarks.indices) {
                val landmark = landmarks[j]
                data[i][j * numCoordinates] = landmark.x
                data[i][j * numCoordinates + 1] = landmark.y
                data[i][j * numCoordinates + 2] = landmark.z
            }
        }

        return data
    }

    private fun convertBitmapToMPImage(bitmap: Bitmap): MPImage {
        // Bitmap을 ByteBuffer로 변환
        val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(byteBuffer)
        byteBuffer.rewind()

        // ByteBuffer를 사용하여 ImageProxy를 생성 (임시적인 ImageReader 사용)
        val imageReader = ImageReader.newInstance(bitmap.width, bitmap.height, ImageFormat.YUV_420_888, 2)
        val image = imageReader.acquireNextImage()

        // ImageProxy에서 MPImage로 변환
        val mpImage = MPImage(image)

        // ImageReader와 Image 해제
        image.close()
        imageReader.close()

        return mpImage
    }

    private fun runInference(input: FloatArray): FloatArray {
        val inputArray = arrayOf(input)
        val outputArray = Array(1) { FloatArray(10) }
        tflite.run(inputArray, outputArray)
        return outputArray[0]
    }

    override fun onBackPressed() {
        finish()
    }
}
