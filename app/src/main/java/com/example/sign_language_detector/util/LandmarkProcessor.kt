package com.example.sign_language_detector.util

import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import kotlin.math.acos
import kotlin.math.sqrt

class LandmarkProcessor {

    private val poseLandmarkIndices = listOf(
        0, 2, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24
    )

    fun processLandmarks(
        resultHandBundle: HandLandmarkerHelper.ResultBundle,
        resultPoseBundle: PoseLandmarkerHelper.ResultBundle,
    ) : FloatArray {
        // 21개의 행, 2개의 열
        val jointLeftHands = Array(21) { FloatArray(2) }
        val jointRightHands = Array(21) { FloatArray(2) }
        val jointPose = Array(21) { FloatArray(2) }

        resultHandBundle.results.forEach { result ->
            result.landmarks().forEachIndexed { i, hand ->
                val handedness = result.handedness()[i].first().categoryName()

                // 손의 핸디드니스에 따라 적절한 배열에 값을 저장
                if (handedness == "Left") {
                    hand.forEachIndexed { j, lm ->
                        jointLeftHands[j] = floatArrayOf(lm.x(), lm.y())
                    }
                } else if (handedness == "Right") {
                    hand.forEachIndexed { j, lm ->
                        jointRightHands[j] = floatArrayOf(lm.x(), lm.y())
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
                            lm.x(), lm.y()
                        )
                    }
                }
            }
        }

        // 각도 계산
        val leftHandAngles = angleHands(jointLeftHands)
        val rightHandAngles = angleHands(jointRightHands)
        val poseAngles = anglePose(jointPose)

        // 모든 랜드마크와 각도 데이터를 하나의 배열로 결합
        val jointData = jointLeftHands.flatMap { it.toList() } +
                jointRightHands.flatMap { it.toList() } +
                jointPose.flatMap { it.toList() }

        return (jointData + leftHandAngles.toList() + rightHandAngles.toList() + poseAngles.toList()).toFloatArray()
    }

    // 각도 처리
    private fun angleHands(jointHands: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 1, 2, 3, 0, 5, 6, 7, 0, 9, 10, 11, 0, 13, 14, 15, 0, 17, 18, 19)
        val v2Indices = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

        return calculateAngles(jointHands, v1Indices, v2Indices)
    }

    private fun anglePose(jointPose: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 2, 0, 1, 0, 0, 7, 8, 8, 8, 10, 12, 12, 12, 7, 7, 9, 11, 11, 11)
        val v2Indices = arrayOf(2, 4, 1, 3, 5, 6, 8, 7, 10, 20, 12, 14, 16, 18, 9, 19, 11, 13, 15, 17)

        return calculateAngles(jointPose, v1Indices, v2Indices)
    }

    private fun calculateAngles(joints: Array<FloatArray>, v1Indices: Array<Int>, v2Indices: Array<Int>): FloatArray {
        val v = Array(v1Indices.size) { FloatArray(2) }
        for (i in v.indices) {
            v[i][0] = joints[v2Indices[i]][0] - joints[v1Indices[i]][0] // x좌표 차이
            v[i][1] = joints[v2Indices[i]][1] - joints[v1Indices[i]][1] // y좌표 차이
        }

        val normV = v.map { sqrt(it[0] * it[0] + it[1] * it[1]) }
        return if (normV.all { it == 0f }) {
            FloatArray(15)
        } else {
            for (i in v.indices) {
                if (normV[i] != 0f) {
                    v[i][0] /= normV[i]
                    v[i][1] /= normV[i]
                }
            }

            val dotProduct = FloatArray(15)
            for (i in 0 until 15) {
                dotProduct[i] = (v[i][0] * v[i + 1][0] + v[i][1] * v[i + 1][1])
                    .coerceIn(-1.0f, 1.0f)
            }

            FloatArray(15) { i -> acos(dotProduct[i]) }
        }
    }
}