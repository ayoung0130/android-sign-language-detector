package com.example.sign_language_detector.util

import android.util.Log
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import kotlin.math.acos
import kotlin.math.sqrt

class LandmarkProcessor {

    private val combinedData = mutableListOf<List<Float>>()
    private var isCollectingData = false

    private val poseLandmarkIndices = listOf(
        0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
    )

    fun processLandmarks(
        resultHandBundle: HandLandmarkerHelper.ResultBundle,
        resultPoseBundle: PoseLandmarkerHelper.ResultBundle,
    ) {
        Log.d("Landmark", "resultHandBundle.results: ${resultHandBundle.results}")

        // 손 랜드마크 처리
        if (resultHandBundle.results.isNotEmpty()) {

            val jointLeftHands = Array(21) { FloatArray(4) }
            val jointRightHands = Array(21) { FloatArray(4) }
            val jointPose = Array(21) { FloatArray(4) }

            isCollectingData = true

            resultHandBundle.results.forEach { result ->
                result.landmarks().forEachIndexed { i, hand ->
                    hand.forEachIndexed { j, lm ->
                        val visibility = setVisibility(lm.x(), lm.y())
                        if (result.handedness()[i].first().categoryName() == "Left") {
                            jointRightHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z(), visibility)
                        } else {
                            jointLeftHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z(), visibility)
                        }
                    }
                }
            }

            // 포즈 랜드마크 처리
            resultPoseBundle.results.forEach { result ->
                result.landmarks().forEachIndexed { i, pose ->
                    pose.forEachIndexed { j, lm ->
                        if (i in poseLandmarkIndices) {
                            jointPose[poseLandmarkIndices.indexOf(j)] = floatArrayOf(
                                lm.x(), lm.y(), lm.z(), lm.visibility().orElse(0.0f)
                            )
                        }
                    }
                }
            }

            // 모든 랜드마크 합치기
            val joint =
                jointLeftHands.flatMap { it.toList() } + jointRightHands.flatMap { it.toList() } + jointPose.flatMap { it.toList() }

            val leftHandAngles = angleHands(jointLeftHands)
            val rightHandAngles = angleHands(jointRightHands)
            val poseAngles = anglePose(jointPose)

            // 각도 계산 추가
            val combined =
                joint + leftHandAngles.toList() + rightHandAngles.toList() + poseAngles.toList()

            combinedData.add(combined)

        } else {
            if (isCollectingData) {
                isCollectingData = false
//                Log.d("Landmark", "Combined Data Size: ${combinedData.size}")
//                combinedData.forEachIndexed { index, data ->
//                    Log.d("Landmark", "Frame $index: $data")
//                }

                clearData()
            }
        }
    }

    fun getLandmarkData(): List<List<Float>> = combinedData

    private fun clearData() {
        combinedData.clear()
    }

    // 각도 처리
    private fun angleHands(jointHands: Array<FloatArray>): FloatArray {
        val v1 = arrayOf(
            jointHands[0], jointHands[1], jointHands[2], jointHands[3],
            jointHands[0], jointHands[5], jointHands[6], jointHands[7],
            jointHands[0], jointHands[9], jointHands[10], jointHands[11],
            jointHands[0], jointHands[13], jointHands[14], jointHands[15],
            jointHands[0], jointHands[17], jointHands[18], jointHands[19]
        )
        val v2 = arrayOf(
            jointHands[1], jointHands[2], jointHands[3], jointHands[4],
            jointHands[5], jointHands[6], jointHands[7], jointHands[8],
            jointHands[9], jointHands[10], jointHands[11], jointHands[12],
            jointHands[13], jointHands[14], jointHands[15], jointHands[16],
            jointHands[17], jointHands[18], jointHands[19], jointHands[20]
        )

        val v = Array(20) { FloatArray(3) }
        for (i in v.indices) {
            for (j in 0..2) {
                v[i][j] = v2[i][j] - v1[i][j]
            }
        }

        val normV = FloatArray(20) { 0f }
        for (i in v.indices) {
            normV[i] = sqrt(v[i][0] * v[i][0] + v[i][1] * v[i][1] + v[i][2] * v[i][2])
        }

        return if (normV.all { it == 0f }) {
            FloatArray(15)
        } else {
            for (i in v.indices) {
                if (normV[i] != 0f) {
                    for (j in 0..2) {
                        v[i][j] /= normV[i]
                    }
                }
            }

            val dotProduct = FloatArray(15)
            for (i in dotProduct.indices) {
                dotProduct[i] =
                    (v[i][0] * v[i + 1][0] + v[i][1] * v[i + 1][1] + v[i][2] * v[i + 1][2])
                        .coerceIn(-1.0f, 1.0f)
            }

            val angle = FloatArray(15)
            for (i in angle.indices) {
                angle[i] = acos(dotProduct[i])
            }
            angle
        }
    }

    private fun anglePose(jointPose: Array<FloatArray>): FloatArray {
        val v1 = arrayOf(
            jointPose[0], jointPose[0], jointPose[0], jointPose[0], jointPose[0], jointPose[0],
            jointPose[7], jointPose[8], jointPose[9], jointPose[10], jointPose[13], jointPose[14],
            jointPose[0], jointPose[0], jointPose[5], jointPose[6], jointPose[0], jointPose[0],
            jointPose[17], jointPose[18]
        )
        val v2 = arrayOf(
            jointPose[1],
            jointPose[2],
            jointPose[3],
            jointPose[4],
            jointPose[5],
            jointPose[6],
            jointPose[5],
            jointPose[6],
            jointPose[7],
            jointPose[8],
            jointPose[9],
            jointPose[10],
            jointPose[17],
            jointPose[18],
            jointPose[17],
            jointPose[18],
            jointPose[19],
            jointPose[20],
            jointPose[19],
            jointPose[20]
        )

        val v = Array(20) { FloatArray(3) }
        for (i in v.indices) {
            for (j in 0..2) {
                v[i][j] = v2[i][j] - v1[i][j]
            }
        }

        val normV = FloatArray(20) { 0f }
        for (i in v.indices) {
            normV[i] = sqrt(v[i][0] * v[i][0] + v[i][1] * v[i][1] + v[i][2] * v[i][2])
        }

        return if (normV.all { it == 0f }) {
            FloatArray(15)
        } else {
            for (i in v.indices) {
                if (normV[i] != 0f) {
                    for (j in 0..2) {
                        v[i][j] /= normV[i]
                    }
                }
            }

            val dotProduct = FloatArray(15)
            for (i in dotProduct.indices) {
                dotProduct[i] =
                    (v[i][0] * v[i + 1][0] + v[i][1] * v[i + 1][1] + v[i][2] * v[i + 1][2])
                        .coerceIn(-1.0f, 1.0f)
            }

            val angle = FloatArray(15)
            for (i in angle.indices) {
                angle[i] = acos(dotProduct[i])
            }
            angle
        }
    }

    // Hand 가시성 정보 처리
    private fun setVisibility(x: Float, y: Float, epsilon: Float = 1e-6f): Float {
        return when {
            x <= epsilon && y <= epsilon -> 0f
            x <= epsilon || y <= epsilon -> 0.5f
            else -> 1f
        }
    }
}
