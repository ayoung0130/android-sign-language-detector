package com.example.sign_language_detector.util

import android.content.Context
import com.example.sign_language_detector.repository.HandLandmarkerHelper

class LandmarkProcessor(context: Context) {

//    private val tflite: Interpreter
    private val seqLength = 30

//    init {
//        val model = loadModelFile(context, "sign_language_detect_model.tflite")
//        tflite = Interpreter(model)
//    }
//
//    private fun loadModelFile(context: Context, modelFileName: String): ByteBuffer {
//        val fileDescriptor = context.assets.openFd(modelFileName)
//        val inputStream = fileDescriptor.createInputStream()
//        val byteBuffer = ByteBuffer.allocateDirect(fileDescriptor.declaredLength.toInt())
//        byteBuffer.order(ByteOrder.nativeOrder())
//        inputStream.channel.read(byteBuffer)
//        inputStream.close()
//        byteBuffer.position(0)
//        return byteBuffer
//    }

    fun processLandmarks(landmarkData: List<List<HandLandmarkerHelper.Quadruple<Float, Float, Float, Float>>>) {
//        if (landmarkData.isEmpty()) return "수어 동작을 시작하세요"

        val numFrames = landmarkData.size
        val numLandmarks = landmarkData[0].size
        val inputArray = Array(numFrames) {
            FloatArray(numLandmarks * 4) // x, y, z, visibility
        }

        for (i in landmarkData.indices) {
            for (j in landmarkData[i].indices) {
                val landmark = landmarkData[i][j]
                inputArray[i][j * 4] = landmark.first
                inputArray[i][j * 4 + 1] = landmark.second
                inputArray[i][j * 4 + 2] = landmark.third
                inputArray[i][j * 4 + 3] = landmark.fourth
            }
        }

        val fullSeqData = mutableListOf<Array<FloatArray>>()
        for (seq in 0..(inputArray.size - seqLength) step 10) {
            fullSeqData.add(inputArray.sliceArray(seq until (seq + seqLength)))
        }

//        val inputBuffer = ByteBuffer.allocateDirect(fullSeqData.size * seqLength * numLandmarks * 4 * 4).apply {
//            order(ByteOrder.nativeOrder())
//        }

//        for (sequence in fullSeqData) {
//            for (frame in sequence) {
//                for (value in frame) {
//                    inputBuffer.putFloat(value)
//                }
//            }
//        }

//        val outputBuffer = ByteBuffer.allocateDirect(fullSeqData.size * 4).apply {
//            order(ByteOrder.nativeOrder())
//        }
//
//        tflite.run(inputBuffer, outputBuffer)
//        inputBuffer.clear()

//        outputBuffer.rewind()
//        val yPred = FloatArray(fullSeqData.size * 4)
//        outputBuffer.asFloatBuffer().get(yPred)

//        val predictedClasses = yPred.indices
//            .filter { it % 4 == 0 }
//            .map { yPred.copyOfRange(it, it + 4) }
//            .map { it -> it.withIndex().maxByOrNull { it.value }?.index ?: -1 }
//
//        val finalPrediction = predictedClasses.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: -1
//
//        return if (finalPrediction != -1) actions[finalPrediction] else "수어 동작을 시작하세요"
    }

    companion object {
        val actions = arrayOf("액션1", "액션2", "액션3") // 예시로 사용할 액션 이름들
    }
}
