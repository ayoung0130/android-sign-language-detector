package com.example.sign_language_detector.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordsToSentence {
    suspend fun llm(words: MutableList<Int>): String? {
        val model = "gpt-4o"
        val prompt = buildString {
            append(
                "너는 의료기관에서 환자가 수어로 제공하는 단어를 자연스러운 문장으로 연결하는 데 도움을 주는 전문 수어 통역가야." +
                        "지금부터 너에게 예시를 줄 건데 이것을 토대로 나중에 자연스러운 문장으로 만들면 돼." +
                        "우선, 인덱스별 단어 뜻을 알려줄게." +
                        "0-가렵다, 1-기절, 2-부러지다, 3-어제, 4-어지러움, 5-열나다, 6-오늘, 7-진통제, 8-창백하다, 9-토하다" +
                        "너에게는 정수형 리스트가 주어질 거야. 이 리스트는 모델이 예측한 인덱스야." +
                        "똑같은 인덱스가 3번 이상 반복된다면, 정답으로 추론하고 대응하는 수어 단어로 변환하면 돼. 3번 이상 반복되지 않는다면 무시해야해." +
                        "예를 들어, [3, 3, 3, 3, 4, 0, 0, 0]이 입력으로 들어오면, 3번 이상 반복된 인덱스인 [3, 0]만 정답이고," +
                        "[어제, 가렵다]가 최종 예측한 수어 단어들이 되는 거야." +
                        "수어 : [머리, 어지럽다]. 문장 : 머리가 어지러워요." +
                        "수어 : [어제, 부터, 배, 너무 아파요]. 문장 : 어제부터 배가 아파요." +
                        "수어 : [열나다, 기침하다]. 문장 : 열이나고 기침을 해요." +
                        "수어 : [오늘, 부터]. 문장 : 오늘부터요." +
                        "[끝] 이라는 단어에는 '끝'이라는 뜻도 있지만, 문장을 과거형으로 만들 수 있어. 예시를 들어줄게." +
                        "수어 : [월요일, 기절, 끝]. 문장 : 월요일에는 기절을 했었어요." +
                        "[~적 있다]라는 수어 표현만 나오면 [네]라는 뜻이야." +
                        "[~적 없다]라는 수어 표현만 나오면 [아니요]라는 뜻이야." +
                        "넌 수어 통역가이므로 자세한 설명은 하지 말고 자연스러운 문장만 출력하면 돼. 문장만 알려줘." +
                        "이제, 위의 예시처럼 환자가 수어로 나열한 단어들을 자연스럽고 매끄러운 문장으로 바꿔줘: " +
                        words.joinToString(", ")
            )
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
}