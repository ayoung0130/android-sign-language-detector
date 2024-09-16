package com.ayeong.sign_language_detector.ui.signlanguagevideo

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayeong.sign_language_detector.databinding.ItemSignLanguageVideoBinding

class SignLanguageVideoAdapter :
    ListAdapter<SignLanguageItem, SignLanguageVideoAdapter.SignLanguageViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignLanguageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSignLanguageVideoBinding.inflate(layoutInflater, parent, false)
        return SignLanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SignLanguageViewHolder, position: Int) {
        val signLanguageItem = getItem(position)
        holder.bind(signLanguageItem)
    }

    class SignLanguageViewHolder(private val binding: ItemSignLanguageVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(signLanguageItem: SignLanguageItem) {
            binding.signLanguageVideoItem = signLanguageItem

            // MediaMetadataRetriever를 사용하여 동영상의 특정 프레임을 미리보기로 표시
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(binding.root.context, signLanguageItem.videoUri)
            val bitmap = retriever.getFrameAtTime(
                5L,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) // timeUs초 지점의 프레임
            binding.videoPreviewImage.setImageBitmap(bitmap)

            retriever.release()

            // Play 버튼 클릭 시 VideoView를 사용해 영상 재생
            binding.playButton.setOnClickListener {
                binding.signLanguageVideoView.visibility = View.VISIBLE
                binding.videoPreviewImage.visibility = View.GONE
                binding.signLanguageVideoView.setVideoURI(signLanguageItem.videoUri)
                binding.signLanguageVideoView.start()
            }

            // 비디오 재생 완료 이벤트 처리
            binding.signLanguageVideoView.setOnCompletionListener {
                binding.signLanguageVideoView.visibility = View.GONE
                binding.videoPreviewImage.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SignLanguageItem>() {
            override fun areItemsTheSame(
                oldItem: SignLanguageItem,
                newItem: SignLanguageItem
            ): Boolean {
                return oldItem.videoUri == newItem.videoUri
            }

            override fun areContentsTheSame(
                oldItem: SignLanguageItem,
                newItem: SignLanguageItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
