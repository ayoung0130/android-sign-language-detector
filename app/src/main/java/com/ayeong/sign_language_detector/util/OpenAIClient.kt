package com.ayeong.sign_language_detector.util

import com.ayeong.sign_language_detector.BuildConfig.OPENAI_API_KEY
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class OpenAIRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>
)

data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class OpenAIResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("message") val message: MessageContent
)

data class MessageContent(
    @SerializedName("content") val content: String
)

interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(@Body request: OpenAIRequest): OpenAIResponse
}

object OpenAIClient {
    private const val BASE_URL = "https://api.openai.com/"
    private const val API_KEY = OPENAI_API_KEY

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: OpenAIApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAIApi::class.java)
}
