package com.example.sign_language_detector.util

import android.util.Log
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import java.util.Arrays
import kotlin.math.acos
import kotlin.math.sqrt

class LandmarkProcessor {

    private val combinedData = mutableListOf<List<Float>>()

    private val poseLandmarkIndices = listOf(
        0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
    )

    fun processLandmarks(
        resultHandBundle: HandLandmarkerHelper.ResultBundle,
        resultPoseBundle: PoseLandmarkerHelper.ResultBundle,
    ) {
        // 손 랜드마크 처리
        if (resultHandBundle.results.first().landmarks().isNotEmpty()) {

            val jointLeftHands = Array(21) { FloatArray(3) }
            val jointRightHands = Array(21) { FloatArray(3) }
            val jointPose = Array(21) { FloatArray(3) }

            resultHandBundle.results.forEach { result ->
                result.landmarks().forEachIndexed { i, hand ->
                    hand.forEachIndexed { j, lm ->
                        if (result.handedness()[i].first().categoryName() == "Left") {
                            jointRightHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                        } else {
                            jointLeftHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                        }
                    }
                }
            }

            // 포즈 랜드마크 처리
            resultPoseBundle.results.forEach { result ->
                result.landmarks().forEachIndexed { _, pose ->
                    pose.forEachIndexed { j, lm ->
                        if (j in poseLandmarkIndices) {
                            jointPose[poseLandmarkIndices.indexOf(j)] = floatArrayOf(
                                lm.x(), lm.y(), lm.z()
                            )
                        }
                    }
                }
            }

            Log.d("tag", "왼손: ${jointLeftHands.contentDeepToString()}")
            Log.d("tag", "오른손: ${jointRightHands.contentDeepToString()}")
            Log.d("tag", "포즈: ${jointPose.contentDeepToString()}")

            // 모든 랜드마크 합치기
            val joint =
                jointLeftHands.flatMap { it.toList() } + jointRightHands.flatMap { it.toList() } + jointPose.flatMap { it.toList() }

            val leftHandAngles = angleHands(jointLeftHands)
            val rightHandAngles = angleHands(jointRightHands)
            val poseAngles = anglePose(jointPose)

            Log.d("tag", "왼손 각도: ${leftHandAngles.contentToString()}")
            Log.d("tag", "오른손 각도: ${rightHandAngles.contentToString()}")
            Log.d("tag", "포즈 각도: ${poseAngles.contentToString()}")

            // 각도 계산 추가
            val combined =
                joint + leftHandAngles.toList() + rightHandAngles.toList() + poseAngles.toList()

            Log.d("tag", "처리된 데이터: $combined")

            combinedData.add(combined)

        } else {
            clearData()
        }
    }

    fun getLandmarkData(): List<List<Float>> {
        Log.d("Landmark", "$combinedData")
        return combinedData
    }

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
            jointPose[0], jointPose[2], jointPose[0], jointPose[1], jointPose[0], jointPose[0],
            jointPose[7], jointPose[8], jointPose[8], jointPose[8], jointPose[10], jointPose[12],
            jointPose[12], jointPose[12], jointPose[7], jointPose[7], jointPose[9], jointPose[11],
            jointPose[11], jointPose[11]
        )
        val v2 = arrayOf(
            jointPose[2],
            jointPose[4],
            jointPose[1],
            jointPose[3],
            jointPose[5],
            jointPose[6],
            jointPose[8],
            jointPose[7],
            jointPose[10],
            jointPose[20],
            jointPose[12],
            jointPose[14],
            jointPose[16],
            jointPose[18],
            jointPose[9],
            jointPose[19],
            jointPose[11],
            jointPose[13],
            jointPose[15],
            jointPose[17]
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

}
