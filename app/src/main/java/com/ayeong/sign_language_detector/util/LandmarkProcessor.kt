package com.ayeong.sign_language_detector.util

import android.util.Log
import com.ayeong.sign_language_detector.repository.HandLandmarkerHelper
import com.ayeong.sign_language_detector.repository.PoseLandmarkerHelper
import kotlin.math.acos
import kotlin.math.sqrt

class LandmarkProcessor {

    // 포즈 랜드마크 중 사용할 인덱스 리스트
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

        // 손 랜드마크 처리
        handResultBundle.results.forEach { result ->
            result.landmarks().forEachIndexed { i, hand ->
                val isLeftHand = result.handedness()[i].first().categoryName() == "Left"
                hand.forEachIndexed { j, lm ->
                    val jointArray = if (isLeftHand) jointRightHands else jointLeftHands
                    jointArray[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                }
            }
        }

        // 포즈 랜드마크 처리
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

        // 각도 계산
        val leftHandAngle = angleHands(jointLeftHands)
        val rightHandAngle = angleHands(jointRightHands)
        val poseAngle = anglePose(jointPose)

        // 모든 랜드마크와 각도 데이터를 하나의 배열로 결합
        val jointData = (jointLeftHands.flatMap { it.toList() } +
                jointRightHands.flatMap { it.toList() } +
                jointPose.flatMap { it.toList() }).toFloatArray()

        val data = (jointData + leftHandAngle + rightHandAngle + poseAngle)
        Log.d("LandmarkProcessor", "data size: ${data.size}")

        return data
    }

    // 손 각도 처리
    private fun angleHands(jointHands: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 1, 2, 3, 0, 5, 6, 7, 0, 9, 10, 11, 0, 13, 14, 15, 0, 17, 18, 19)
        val v2Indices =
            arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

        return calculateAngles(jointHands, v1Indices, v2Indices)
    }

    // 포즈 각도 처리
    private fun anglePose(jointPose: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 2, 0, 1, 0, 0, 7, 8,  8,  8, 10, 12, 12, 12, 7,  7,  9, 11, 11, 11)
        val v2Indices = arrayOf(2, 4, 1, 3, 5, 6, 8, 7, 10, 20, 12, 14, 16, 18, 9, 19, 11, 13, 15, 17)

        return calculateAngles(jointPose, v1Indices, v2Indices)
    }

    // 각도 계산 함수
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
        val angles = FloatArray(15)

        val firstAngleIndices = arrayOf(0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14, 16, 17, 18)
        val secondAngleIndices = arrayOf(1, 2, 3, 5, 6, 7, 9, 10, 11, 13, 14, 15, 17, 18, 19)

        // 벡터 정규화 및 각도 계산
        for (i in angles.indices) {
            val v1 = v[firstAngleIndices[i]]
            val v2 = v[secondAngleIndices[i]]

            // 벡터의 크기(노름) 계산
            val normV1 = sqrt(v1.map { it * it }.sum())
            val normV2 = sqrt(v2.map { it * it }.sum())

            angles[i] = if (normV1 == 0f || normV2 == 0f) {
                0f
            } else {
                // 도트 곱 계산, acos으로 각도 계산
                val dotProduct = v1.zip(v2).map { (a, b) -> a * b }.sum() / (normV1 * normV2)
                acos(dotProduct.coerceIn(-1f, 1f))
            }
        }

        return angles
    }
}
