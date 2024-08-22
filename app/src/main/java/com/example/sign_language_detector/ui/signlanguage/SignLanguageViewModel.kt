package com.example.sign_language_detector.ui.signlanguage

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignLanguageViewModel : ViewModel() {

    private val _videoUris = MutableLiveData<List<Uri>>()
    val videoUris: LiveData<List<Uri>> = _videoUris

    var navigateBack: (() -> Unit)? = null

    val adapter = SignLanguageAdapter()

    fun setVideoUris(uris: List<Uri>) {
        _videoUris.value = uris
        adapter.submitList(uris)
    }

    fun onBackButtonClick() {
        navigateBack?.invoke()
    }
}
