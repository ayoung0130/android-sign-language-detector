package com.ayeong.sign_language_detector.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ayeong.sign_language_detector.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var handResults: HandLandmarkerResult? = null
    private var poseResults: PoseLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var guideLinePaint = Paint()

    init {
        initPaints()
    }

    private fun initPaints() {
        // 랜드마크 연결 선
        linePaint.color = ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        // 랜드마크 점
        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 손 랜드마크 그리기
        handResults?.let { drawHandlandmark(it, canvas) }

        // 포즈 랜드마크 그리기
        poseResults?.let { drawPoseLandmark(it, canvas) }
    }

    private fun drawHandlandmark(result: HandLandmarkerResult, canvas: Canvas) {
        result.let { handLandmarkerResult ->
            handLandmarkerResult.landmarks().forEachIndexed { index, landmark ->
                val handedness = handLandmarkerResult.handedness()[index].first().categoryName()
                val lineColor = if (handedness == "Right") Color.GREEN else Color.YELLOW
                val handLinePaint = Paint().apply {
                    color = lineColor
                    strokeWidth = LANDMARK_STROKE_WIDTH
                    style = Paint.Style.STROKE
                }

                // 손 랜드마크 그리기
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                // 손 연결 그리기
                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        landmark[it!!.start()].x() * imageWidth * scaleFactor,
                        landmark[it.start()].y() * imageHeight * scaleFactor,
                        landmark[it.end()].x() * imageWidth * scaleFactor,
                        landmark[it.end()].y() * imageHeight * scaleFactor,
                        handLinePaint
                    )
                }
            }
        }
    }

    private fun drawPoseLandmark(result: PoseLandmarkerResult, canvas: Canvas) {
        result.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks()[0][it!!.start()].x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks()[0][it.start()].y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks()[0][it.end()].x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks()[0][it.end()].y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }
        }
    }

    fun setHandResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ) {
        handResults = handLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        updateScaleFactor(runningMode)
        invalidate()
    }

    fun setPoseResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ) {
        poseResults = poseLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        updateScaleFactor(runningMode)
        invalidate()
    }

    private fun updateScaleFactor(runningMode: RunningMode) {
        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }

            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }
}
