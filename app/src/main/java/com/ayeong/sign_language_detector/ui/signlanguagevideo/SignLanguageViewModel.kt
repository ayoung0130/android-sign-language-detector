package com.ayeong.sign_language_detector.ui.signlanguagevideo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignLanguageViewModel : ViewModel() {

    private val _signLanguageItems = MutableLiveData<List<SignLanguageItem>>()
    val signLanguageItems: LiveData<List<SignLanguageItem>> = _signLanguageItems

    var navigateBack: (() -> Unit)? = null

    val adapter = SignLanguageAdapter()

    fun loadSignLanguageItems(context: Context) {
        _signLanguageItems.value = SignLanguageList.getSignLanguageItems(context)
        adapter.submitList(_signLanguageItems.value)
    }

    fun onBackButtonClick() {
        navigateBack?.invoke()
    }
}
