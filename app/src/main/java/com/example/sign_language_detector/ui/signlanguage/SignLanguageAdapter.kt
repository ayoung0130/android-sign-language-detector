package com.example.sign_language_detector.ui.signlanguage

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sign_language_detector.databinding.ItemSignLanguageBinding

class SignLanguageAdapter :
    ListAdapter<Uri, SignLanguageAdapter.SignLanguageViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignLanguageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSignLanguageBinding.inflate(layoutInflater, parent, false)
        return SignLanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SignLanguageViewHolder, position: Int) {
        val videoUri = getItem(position)
        holder.bind(videoUri)
    }

    class SignLanguageViewHolder(private val binding: ItemSignLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(videoUri: Uri) {
            binding.videoUri = videoUri

            // Play 버튼 클릭 시 영상 재생
            binding.playButton.setOnClickListener {
                binding.signLanguageVideoView.setVideoURI(videoUri)
                binding.signLanguageVideoView.start()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Uri>() {
            override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
                return oldItem == newItem
            }
        }
    }
}
