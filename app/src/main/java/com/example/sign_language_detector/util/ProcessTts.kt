package com.example.sign_language_detector.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class ProcessTts(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    // TTS 초기화 시 호출되는 콜백 함수
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale.KOREAN  // 원하는 언어 설정
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported or missing data")
            } else {
                isInitialized = true
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    // TTS로 텍스트를 읽는 함수
    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TTS", "TTS not initialized")
        }
    }

    // TTS 자원 해제 함수
    fun shutdown() {
        tts?.shutdown()
    }
}