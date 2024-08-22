package com.example.sign_language_detector.util

import android.util.Log
import android.widget.Toast
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.acos
import kotlin.math.sqrt

class LandmarkProcessor {

    private val poseLandmarkIndices = listOf(
        0, 2, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24
    )

    fun processLandmarks(
        handResultBundle: HandLandmarkerHelper.ResultBundle,
        poseResultBundle: PoseLandmarkerHelper.ResultBundle
    ): FloatArray {

        val jointLeftHands = Array(21) { FloatArray(3) }
        val jointRightHands = Array(21) { FloatArray(3) }
        val jointPose = Array(21) { FloatArray(3) }

        handResultBundle.results.forEach { result ->
            result.landmarks().forEachIndexed { i, hand ->
                // 손의 핸디드니스에 따라 적절한 배열에 값을 저장
                if (result.handedness()[i].first().categoryName() == "Left") {
                    hand.forEachIndexed { j, lm ->
                        jointRightHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                    }
                } else {
                    hand.forEachIndexed { j, lm ->
                        jointLeftHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                    }
                }
            }
        }

        poseResultBundle.results.forEach { result ->
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

        val leftHandAngle = angleHands(jointLeftHands)
        val rightHandAngle = angleHands(jointRightHands)
        val poseAngle = anglePose(jointPose)

        // 모든 랜드마크와 각도 데이터를 하나의 배열로 결합
        val jointData = (jointLeftHands.flatMap { it.toList() } +
                jointRightHands.flatMap { it.toList() } +
                jointPose.flatMap { it.toList() }).toFloatArray()

        val data = (jointData + leftHandAngle + rightHandAngle + poseAngle)
        Log.d("tag", "data size: ${data.size}")

        return data
    }

    // 각도 처리
    private fun angleHands(jointHands: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 1, 2, 3, 0, 5, 6, 7, 0, 9, 10, 11, 0, 13, 14, 15, 0, 17, 18, 19)
        val v2Indices =
            arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

        return calculateAngles(jointHands, v1Indices, v2Indices)
    }

    private fun anglePose(jointPose: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 2, 0, 1, 0, 0, 7, 8, 8, 8, 10, 12, 12, 12, 7, 7, 9, 11, 11, 11)
        val v2Indices =
            arrayOf(2, 4, 1, 3, 5, 6, 8, 7, 10, 20, 12, 14, 16, 18, 9, 19, 11, 13, 15, 17)

        return calculateAngles(jointPose, v1Indices, v2Indices)
    }

    private fun calculateAngles(
        joints: Array<FloatArray>,
        v1Indices: Array<Int>,
        v2Indices: Array<Int>
    ): FloatArray {
        // 각 관절 간의 벡터 계산
        val v = Array(v1Indices.size) { i ->
            FloatArray(3) { j ->
                joints[v2Indices[i]][j] - joints[v1Indices[i]][j]
            }
        }

        // 벡터 정규화 및 각도 계산
        val angles = FloatArray(15)
        val angleIndices = arrayOf(
            0, 1, 2, 4, 5, 6, 8, 9, 10,
            12, 13, 14, 16, 17, 18
        )

        for (i in angleIndices.indices) {
            val idx = angleIndices[i]
            val v1 = v[idx]
            val v2 = v[idx + 1]

            val normV1 = sqrt(v1.map { it * it }.sum())
            val normV2 = sqrt(v2.map { it * it }.sum())

            if (normV1 == 0f || normV2 == 0f) {
                angles[i] = 0f
            } else {
                val dotProduct =
                    v1.zip(v2).sumOf { (a, b) -> (a * b).toDouble() }.toFloat() / (normV1 * normV2)
                val clippedDotProduct = dotProduct.coerceIn(-1f, 1f)
                angles[i] = acos(clippedDotProduct)
            }
        }

        return angles
    }
}