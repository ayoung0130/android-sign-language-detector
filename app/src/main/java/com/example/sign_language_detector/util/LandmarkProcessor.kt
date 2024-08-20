package com.example.sign_language_detector.util

import kotlin.math.acos
import kotlin.math.sqrt

class LandmarkProcessor {

    // 각도 처리
    fun angleHands(jointHands: Array<FloatArray>): FloatArray {
        val v1Indices = arrayOf(0, 1, 2, 3, 0, 5, 6, 7, 0, 9, 10, 11, 0, 13, 14, 15, 0, 17, 18, 19)
        val v2Indices = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

        return calculateAngles(jointHands, v1Indices, v2Indices)
    }

    fun anglePose(jointPose: Array<FloatArray>): FloatArray {
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