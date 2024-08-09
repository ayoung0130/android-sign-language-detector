package com.example.sign_language_detector.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sign_language_detector.databinding.ActivityMainBinding
import com.example.sign_language_detector.util.wordsToSentence
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val words = listOf("어제", "어지러움", "아프다", "끝")

        lifecycleScope.launch {
            val sentence = wordsToSentence(words)
            if (sentence != null) {
                Log.d("llm/tts", "문장 변환 결과: $sentence")
            } else {
                Log.d("llm/tts", "문장 변환에 실패했습니다.")
            }
        }
    }

}