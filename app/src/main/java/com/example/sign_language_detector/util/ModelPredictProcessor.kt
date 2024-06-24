package com.example.sign_language_detector.util

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelPredictProcessor(context: Context) {

    private val tflite: Interpreter

    init {
        val model = loadModelFile(context, "sign_language_detect_model.tflite")
        tflite = Interpreter(model)
    }

    private fun loadModelFile(context: Context, modelFileName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = fileDescriptor.createInputStream()
        val byteBuffer = ByteBuffer.allocateDirect(fileDescriptor.declaredLength.toInt())
        byteBuffer.order(ByteOrder.nativeOrder())
        inputStream.channel.read(byteBuffer)
        inputStream.close()
        byteBuffer.position(0)
        return byteBuffer
    }

    fun predict(sequenceData: List<List<Float>>): String {
        val inputSequences = createSequences(sequenceData, 30, 10)
        val predictions = mutableListOf<Int>()

        for (sequence in inputSequences) {
            val inputArray = Array(1) { sequence.toTypedArray() }
            val outputArray = Array(1) { FloatArray(actions.size) }
            tflite.run(inputArray, outputArray)
            predictions.add(outputArray[0].indexOfMax())
        }

        val finalPrediction =
            predictions.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return actions[finalPrediction ?: 0]
    }

    private fun createSequences(
        data: List<List<Float>>,
        sequenceLength: Int,
        step: Int
    ): List<List<List<Float>>> {
        val sequences = mutableListOf<List<List<Float>>>()
        for (i in 0..data.size - sequenceLength step step) {
            val sequence = data.subList(i, i + sequenceLength)
            sequences.add(sequence)
        }
        return sequences
    }

    private fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        for (i in indices) {
            if (this[i] > this[maxIndex]) maxIndex = i
        }
        return maxIndex
    }

    companion object {
        val actions = arrayOf("가렵다", "기절", "부러지다", "어제", "어지러움", "열나다", "오늘", "진통제", "창백하다", "토하다")
    }
}