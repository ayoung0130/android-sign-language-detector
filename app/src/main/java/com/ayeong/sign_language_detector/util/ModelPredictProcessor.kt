package com.ayeong.sign_language_detector.util

import android.content.Context
import android.util.Log
import com.ayeong.sign_language_detector.Constants
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

    fun predict(data: List<FloatArray>): MutableList<String> {
        // 전체 시퀀스를 생성
        val inputSequences = createSequences(data, Constants.SLICING_WINDOW, 5)

        // TFLite 모델에 입력할 배열 준비
        val inputArray = Array(inputSequences.size) { index ->
            inputSequences[index].toTypedArray()
        }
        val outputArray = Array(inputSequences.size) { FloatArray(Constants.ACTIONS.size) }

        // 모델에 전체 시퀀스를 전달하여 예측 수행
        tflite.run(inputArray, outputArray)

        // outputArray의 내용을 로그로 출력
        outputArray.forEachIndexed { index, output ->
            Log.d("ModelPredictProcessor", "outputArray[$index]: ${output.contentToString()}")
        }

        // 예측 결과 처리
        val predictions = mutableListOf<Int>()
        for (output in outputArray) {
            predictions.add(output.indexOfMax())
        }
        Log.d("ModelPredictProcessor", "predictions: $predictions")

        // 세 번 이상 연속으로 반복된 단어만 필터링
        val filteredPredictions = mutableListOf<String>()
        var currentWord: Int? = null
        var count = 0

        for (prediction in predictions) {
            if (prediction == currentWord) {
                count++
            } else {
                if (count >= 3 && currentWord != null) {
                    filteredPredictions.add(Constants.ACTIONS[currentWord])
                }
                currentWord = prediction
                count = 1
            }
        }
        // 마지막 단어에 대한 처리
        if (count >= 3 && currentWord != null) {
            filteredPredictions.add(Constants.ACTIONS[currentWord])
        }

        Log.d("ModelPredictProcessor", "filteredPredictions: $filteredPredictions")

        return filteredPredictions
    }

    private fun createSequences(
        data: List<FloatArray>,
        slicingWindow: Int,
        jumpingWindow: Int
    ): List<List<FloatArray>> {
        Log.d("ModelPredictProcessor", "Data size: ${data.size}")

        val sequences = mutableListOf<List<FloatArray>>()
        for (i in 0..data.size - slicingWindow step jumpingWindow) {
            val sequence = data.subList(i, i + slicingWindow)
            Log.d("ModelPredictProcessor", "window: $i, ${i + slicingWindow}")
            sequences.add(sequence)

            // 배열의 내용을 출력하도록 수정
            val sequenceContent = sequence.joinToString(", ") { it.contentToString() }
            Log.d("ModelPredictProcessor", "Sequence: $sequenceContent")
        }

        Log.d("ModelPredictProcessor", "Sequence data size: ${sequences.size}")

        return sequences
    }

    private fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        for (i in indices) {
            if (this[i] > this[maxIndex]) maxIndex = i
        }
        return maxIndex
    }
}
