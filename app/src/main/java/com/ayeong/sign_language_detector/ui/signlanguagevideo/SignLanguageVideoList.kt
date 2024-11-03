package com.ayeong.sign_language_detector.ui.signlanguagevideo

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

            SignLanguageItem(Uri.parse("${basePath}idx_10"), "배"),
            SignLanguageItem(Uri.parse("${basePath}idx_11"), "다리"),
            SignLanguageItem(Uri.parse("${basePath}idx_12"), "어깨"),
            SignLanguageItem(Uri.parse("${basePath}idx_13"), "아래"),
            SignLanguageItem(Uri.parse("${basePath}idx_14"), "부터"),
            SignLanguageItem(Uri.parse("${basePath}idx_15"), "목"),
            SignLanguageItem(Uri.parse("${basePath}idx_16"), "머리"),
            SignLanguageItem(Uri.parse("${basePath}idx_17"), "발"),
            SignLanguageItem(Uri.parse("${basePath}idx_18"), "아프다"),
            SignLanguageItem(Uri.parse("${basePath}idx_19"), "위"),

            SignLanguageItem(Uri.parse("${basePath}idx_36"), "화상"),
            SignLanguageItem(Uri.parse("${basePath}idx_21"), "깔리다"),
            SignLanguageItem(Uri.parse("${basePath}idx_22"), "인대"),
            SignLanguageItem(Uri.parse("${basePath}idx_23"), "뼈"),
            SignLanguageItem(Uri.parse("${basePath}idx_24"), "알려주세요"),
            SignLanguageItem(Uri.parse("${basePath}idx_25"), "내일"),
            SignLanguageItem(Uri.parse("${basePath}idx_26"), "피나다"),
            SignLanguageItem(Uri.parse("${basePath}idx_27"), "손"),
            SignLanguageItem(Uri.parse("${basePath}idx_28"), "월요일"),
            SignLanguageItem(Uri.parse("${basePath}idx_29"), "화요일"),

            SignLanguageItem(Uri.parse("${basePath}idx_30"), "수요일"),
            SignLanguageItem(Uri.parse("${basePath}idx_31"), "목요일"),
            SignLanguageItem(Uri.parse("${basePath}idx_32"), "금요일"),
            SignLanguageItem(Uri.parse("${basePath}idx_33"), "토요일"),
            SignLanguageItem(Uri.parse("${basePath}idx_34"), "일요일"),
            SignLanguageItem(Uri.parse("${basePath}idx_35"), "적 있다(예)"),
            SignLanguageItem(Uri.parse("${basePath}idx_20"), "적 없다(아니오)"),
            SignLanguageItem(Uri.parse("${basePath}idx_37"), "병원"),
            SignLanguageItem(Uri.parse("${basePath}idx_39"), "의사"),
            SignLanguageItem(Uri.parse("${basePath}idx_38"), "끝(과거형 표현)"),
        )
    }
}

data class SignLanguageItem(
    val videoUri: Uri,
    val actionName: String
)