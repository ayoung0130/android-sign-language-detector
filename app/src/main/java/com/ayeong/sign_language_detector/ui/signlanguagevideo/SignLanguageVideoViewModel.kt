package com.ayeong.sign_language_detector.ui.signlanguagevideo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignLanguageVideoViewModel : ViewModel() {

    private val _signLanguageItems = MutableLiveData<List<SignLanguageItem>>()
    val signLanguageItems: LiveData<List<SignLanguageItem>> = _signLanguageItems

    var navigateBack: (() -> Unit)? = null

    val adapter = SignLanguageVideoAdapter { item ->
        playVideo(item.videoUri)  // 클릭된 아이템의 URI를 playVideo 함수에 전달
    }

    private val _selectedVideoUri = MutableLiveData<Uri>()
    val selectedVideoUri: LiveData<Uri?> = _selectedVideoUri

    fun loadSignLanguageItems(context: Context) {
        val items = SignLanguageList.getSignLanguageItems(context)
        adapter.submitList(items)  // submitList를 통해 리스트 업데이트
    }

    private fun playVideo(uri: Uri) {
        _selectedVideoUri.value = uri
    }

    fun onBackButtonClick() {
        navigateBack?.invoke()
    }

    // 닫기 버튼 클릭 시 selectedVideoUri를 null로 설정하여 영상을 숨기기
    fun onCloseButtonClick() {
        _selectedVideoUri.value = null
    }

    // 재생 버튼 클릭 시 현재 선택된 영상이 있으면 다시 재생
    fun onPlayButtonClick() {
        _selectedVideoUri.value?.let { playVideo(it) }
    }
}
