package com.example.sign_language_detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    var maxNumHands: Int = DEFAULT_NUM_HANDS,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    // 이 리스너는 RunningMode.LIVE_STREAM에서만 사용됩니다
    val handLandmarkerHelperListener: LandmarkerListener? = null
) {

    // 이 예제에서는 변경될 수 있으므로 var로 설정해야 합니다.
    // Hand Landmarker가 변경되지 않는다면 lazy val를 사용하는 것이 좋습니다.
    private var handLandmarker: HandLandmarker? = null

    // 랜드마크 좌표값 저장을 위한 리스트
    private val leftHandData = mutableListOf<List<Quadruple<Float, Float, Float, Float>>>()
    private val rightHandData = mutableListOf<List<Quadruple<Float, Float, Float, Float>>>()

    private var isCollectingData = false

    init {
        setupHandLandmarker()
    }

    // HandLandmarkerHelper의 실행 상태를 반환합니다
    fun isClose(): Boolean {
        return handLandmarker == null
    }

    // 현재 설정을 사용하여 Hand landmarker를 초기화합니다.
    // CPU는 메인 스레드에서 생성되고 백그라운드 스레드에서 사용되는 Landmarker와 함께 사용할 수 있지만,
    // GPU 대리자는 Landmarker를 초기화한 스레드에서 사용해야 합니다.
    fun setupHandLandmarker() {
        // 일반적인 hand landmarker 옵션 설정
        val baseOptionBuilder = BaseOptions.builder()

        // 모델 실행에 지정된 하드웨어를 사용합니다. 기본값은 CPU입니다.
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionBuilder.setDelegate(Delegate.CPU)
            }
            DELEGATE_GPU -> {
                baseOptionBuilder.setDelegate(Delegate.GPU)
            }
        }

        baseOptionBuilder.setModelAssetPath(MP_HAND_LANDMARKER_TASK)

        // runningMode가 handLandmarkerHelperListener와 일치하는지 확인합니다.
        when (runningMode) {
            RunningMode.LIVE_STREAM -> {
                if (handLandmarkerHelperListener == null) {
                    throw IllegalStateException(
                        "runningMode가 LIVE_STREAM일 때 handLandmarkerHelperListener가 설정되어야 합니다."
                    )
                }
            }
            else -> {
                // 아무 작업도 수행하지 않습니다.
            }
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            // base 옵션과 Hand Landmarker에만 사용되는 특정 옵션으로 옵션 빌더를 생성합니다.
            val optionsBuilder =
                HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinHandDetectionConfidence(minHandDetectionConfidence)
                    .setMinTrackingConfidence(minHandTrackingConfidence)
                    .setMinHandPresenceConfidence(minHandPresenceConfidence)
                    .setNumHands(maxNumHands)
                    .setRunningMode(runningMode)

            // ResultListener와 ErrorListener는 LIVE_STREAM 모드에서만 사용됩니다.
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            handLandmarker =
                HandLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            handLandmarkerHelperListener?.onHandError(
                "Hand Landmarker 초기화에 실패했습니다. 오류 로그를 참조하세요."
            )
            Log.e(
                TAG, "MediaPipe가 오류로 인해 태스크를 로드하지 못했습니다: " + e.message
            )
        } catch (e: RuntimeException) {
            // GPU를 지원하지 않는 모델을 사용할 때 발생합니다.
            handLandmarkerHelperListener?.onHandError(
                "Hand Landmarker 초기화에 실패했습니다. 오류 로그를 참조하세요.", GPU_ERROR
            )
            Log.e(
                TAG,
                "이미지 분류기가 모델 로드에 실패했습니다: " + e.message
            )
        }
    }

    // ImageProxy를 MP Image로 변환하고 HandlandmarkerHelper에 전달합니다.
    fun detectLiveStream(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean
    ) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException(
                "RunningMode.LIVE_STREAM이 아닌 상태에서 detectLiveStream을 호출하려고 합니다."
            )
        }
        val frameTime = SystemClock.uptimeMillis()

        // 프레임에서 RGB 비트를 복사하여 비트맵 버퍼에 저장
        val bitmapBuffer =
            Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
//        imageProxy.close()

        val matrix = Matrix().apply {
            // 카메라에서 받은 프레임을 표시되는 방향과 동일하게 회전
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // 사용자가 전면 카메라를 사용할 경우 이미지 반전
            if (isFrontCamera) {
                postScale(
                    -1f,
                    1f,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat()
                )
            }
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        // 입력 Bitmap 객체를 MPImage 객체로 변환하여 추론 실행
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        detectAsync(mpImage, frameTime)
    }

    // MediaPipe Hand Landmarker API를 사용하여 손 랜드마크 실행
    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        handLandmarker?.detectAsync(mpImage, frameTime)
        // RunningMode.LIVE_STREAM을 사용하므로, 랜드마크 결과는 returnLivestreamResult 함수에서 반환됩니다.
    }

    // 이 HandLandmarkerHelper의 호출자에게 랜드마크 결과 반환
    private fun returnLivestreamResult(
        result: HandLandmarkerResult,
        input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        if(result.landmarks().isNotEmpty()) {
            isCollectingData = true

            // 랜드마크 좌표값 수집
            result.landmarks().forEachIndexed { index, hand ->
                val landmarks = hand.map { lm ->
                    val visibility = setVisibility(lm.x(), lm.y())
                    Quadruple(lm.x(), lm.y(), lm.z(), visibility)
                }
                val handedness = result.handedness()[index].first().categoryName()
                if (handedness == "Right") {
                    leftHandData.add(landmarks)
                } else if (handedness == "Left") {
                    rightHandData.add(landmarks)
                }
            }
        } else {
            if (isCollectingData) {
                isCollectingData = false
                val combinedData = combineHandDataList()
                Log.d("좌표값", "Combined Data: ${combinedData.joinToString()}")
                Log.d("좌표값", "Left Hand Data Size: ${leftHandData.size}")
                Log.d("좌표값", "Right Hand Data Size: ${rightHandData.size}")
                clearHandData()
            }
        }

        handLandmarkerHelperListener?.onHandResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    fun getLeftHandData(): List<List<Quadruple<Float, Float, Float, Float>>> = leftHandData
    fun getRightHandData(): List<List<Quadruple<Float, Float, Float, Float>>> = rightHandData

    private fun clearHandData() {
        leftHandData.clear()
        rightHandData.clear()
    }

    private fun combineHandDataList(): List<List<Float>> {
        val combinedData = mutableListOf<List<Float>>()
        val size = maxOf(leftHandData.size, rightHandData.size)
        for (i in 0 until size) {
            val leftData = if (i < leftHandData.size) leftHandData[i] else List(21) { Quadruple(0f, 0f, 0f, 0f) }
            val rightData = if (i < rightHandData.size) rightHandData[i] else List(21) { Quadruple(0f, 0f, 0f, 0f) }
            combinedData.add((leftData + rightData).flatMap { listOf(it.first, it.second, it.third,
                it.fourth
            ) })
        }
        return combinedData
    }

    // 이 HandLandmarkerHelper의 호출자에게 감지 중 발생한 오류 반환
    private fun returnLivestreamError(error: RuntimeException) {
        handLandmarkerHelperListener?.onHandError(
            error.message ?: "알 수 없는 오류가 발생했습니다"
        )
    }

    companion object {
        const val TAG = "HandLandmarkerHelper"
        private const val MP_HAND_LANDMARKER_TASK = "hand_landmarker.task"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_HANDS = 2
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }

    data class ResultBundle(
        val results: List<HandLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onHandError(error: String, errorCode: Int = OTHER_ERROR)
        fun onHandResults(resultBundle: ResultBundle)
    }

    // x, y, z 좌표와 가시성 정보를 저장하는 데이터 클래스
    data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    // 각도 처리


    // 가시성 정보 처리
    private fun setVisibility(x: Float, y: Float, epsilon: Float = 1e-6f): Float {
        return when {
            x <= epsilon && y <= epsilon -> 0f
            x <= epsilon || y <= epsilon -> 0.5f
            else -> 1f
        }
    }
}
