package com.ayeong.sign_language_detector.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordsToSentence {
    suspend fun llm(words: MutableList<String>): String? {
        val model = "gpt-4o"
        val prompt = buildString {
            append(
                "너는 의료기관에서 환자가 수어로 제공하는 단어를 자연스러운 문장으로 연결하는 데 도움을 주는 전문 수어 통역가야." +
                        "지금부터 너에게 예시를 보여줄 거야. 이것을 토대로 자연스러운 문장으로 만들면 돼." +
                        "너에게는 문자 리스트가 주어질 거야. 이 리스트는 모델이 예측한 단어 리스트야." +

                        "똑같은 단어가 연속으로 3번 이상 반복된다면 '정답 리스트'에 넣어. 같은 단어가 연속으로 3번 이상 반복되지 않는다면 무시해야해." +
                        "예를 들어, [어제, 어제, 어제, 어제, 열나다, 가렵다, 가렵다, 가렵다]가 입력으로 들어오면, 3번 이상 반복된 단어인" +
                        "[어제, 가렵다]가 '정답 리스트'가 되는 거야. 이제 정답 리스트에 따른 문장 예문을 보여줄게" +
                        "수어 : [머리, 어지럽다]. 문장 : 머리가 어지러워요." +
                        "수어 : [어제, 부터, 배, 아파요]. 문장 : 어제부터 배가 아파요." +
                        "수어 : [열나다, 창백하다]. 문장 : 열이나고 창백해요." +
                        "수어 : [월요일, 부터]. 문장 : 월요일부터요." +
                        "수어 : [다리, 깔리다, 뼈, 부러지다, 끝]. 문장 : 다리가 깔려서 뼈가 부러졌어요." +
                        "단어 하나만 정답 리스트에 들어오면, 그냥 그 단어를 문장식으로 만들어. 이상한 다른 단어 임의로 포함하지마" +
                        "[끝] 이라는 단어는 문장을 과거형으로 만들 수 있어. 예시를 들어줄게." +
                        "수어 : [월요일, 기절, 끝]. 문장 : 월요일에 기절을 했어요." +

                        "넌 수어 통역가이므로 설명은 필요 없어. 자연스러운 문장만 알려줘" +
                        "모든 단어가 연속으로 3번 이상 반복되지 않는다면, '다시 한번 동작해주세요'라고 해" +
                        "모델이 예측한 단어가 아닌 다른 임의의 단어를 마음대로 문장에 포함해서는 안돼." +
                        "이제, 위의 예시처럼 환자가 수어로 나열한 단어들을 자연스럽고 매끄러운 문장으로 바꿔줘: " +
                        "다시 강조하지만, 3번 이상 반복된 단어는 반드시 포함하고, 그 외의 단어는 무시해: " +
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