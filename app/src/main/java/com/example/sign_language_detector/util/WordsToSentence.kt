package com.example.sign_language_detector.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

suspend fun wordsToSentence(words: List<String>): String? {
    val model = "gpt-4o"
    val prompt = buildString {
        append("너는 의료기관에서 환자가 수어로 제공하는 단어를 자연스러운 문장으로 연결하는 데 도움을 주는 전문 수어 통역가야.")
        append("지금부터 너에게 예시를 줄 건데 이것을 토대로 나중에 자연스러운 문장으로 만들면 돼.")
        append("수어 : [머리], [어지럽다]. 문장 : 머리가 어지러워요.")
        append("수어 : [어제], [부터], [배], [너무 아파요]. 문장 : 어제부터 배가 아파요.")
        append("수어 : [열나다], [기침하다]. 문장 : 열이나고 기침을 해요.")
        append("수어 : [오늘], [부터]. 문장 : 오늘부터요.")
        append("[끝] 이라는 단어에는 '끝'이라는 뜻도 있지만, 문장을 과거형으로 만들 수 있어. 예시를 들어줄게.")
        append("수어 : [월요일], [기절], [끝]. 문장 : 월요일에는 기절을 했었어요.")
        append("[~적 있다]라는 수어 표현만 나오면 [네]라는 뜻이야.")
        append("[~적 없다]라는 수어 표현만 나오면 [아니요]라는 뜻이야.")
        append("넌 수어 통역가이므로 자세한 설명은 하지 말고 자연스러운 문장만 출력하면 돼.")
        append("이제, 위의 예시처럼 환자가 수어로 나열한 단어들을 자연스럽고 매끄러운 문장으로 바꿔줘: ")
        append(words.joinToString(", "))
    }

    val request = OpenAIRequest(
        model = model,
        messages = listOf(
            Message(role = "system", content = prompt)
        )
    )

    return withContext(Dispatchers.IO) {
        try {
            val response = OpenAIClient.api.createChatCompletion(request)
            response.choices.firstOrNull()?.message?.content?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}