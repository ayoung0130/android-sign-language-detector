package com.ayeong.sign_language_detector.ui.signlanguage

import android.content.Context
import android.net.Uri

object SignLanguageList {

    private const val BASE_RESOURCE_PATH = "android.resource://"

    fun getSignLanguageItems(context: Context): List<SignLanguageItem> {
        val basePath = "$BASE_RESOURCE_PATH${context.packageName}/raw/"
        return listOf(
            SignLanguageItem(Uri.parse("${basePath}idx_0"), "가렵다"),
            SignLanguageItem(Uri.parse("${basePath}idx_1"), "기절"),
            SignLanguageItem(Uri.parse("${basePath}idx_2"), "부러지다"),
            SignLanguageItem(Uri.parse("${basePath}idx_3"), "어제"),
            SignLanguageItem(Uri.parse("${basePath}idx_4"), "어지러움"),
            SignLanguageItem(Uri.parse("${basePath}idx_5"), "열나다"),
            SignLanguageItem(Uri.parse("${basePath}idx_6"), "오늘"),
            SignLanguageItem(Uri.parse("${basePath}idx_7"), "진통제"),
            SignLanguageItem(Uri.parse("${basePath}idx_8"), "창백하다"),
            SignLanguageItem(Uri.parse("${basePath}idx_9"), "토하다"),

        )
    }
}

data class SignLanguageItem(
    val videoUri: Uri,
    val actionName: String
)