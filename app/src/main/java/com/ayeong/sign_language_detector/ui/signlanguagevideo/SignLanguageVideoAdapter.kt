package com.ayeong.sign_language_detector.ui.signlanguagevideo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayeong.sign_language_detector.databinding.ItemSignLanguageVideoBinding

class SignLanguageVideoAdapter(
    private val onItemClick: (SignLanguageItem) -> Unit
) : ListAdapter<SignLanguageItem, SignLanguageVideoAdapter.SignLanguageViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignLanguageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSignLanguageVideoBinding.inflate(layoutInflater, parent, false)
        return SignLanguageViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SignLanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SignLanguageViewHolder(
        private val binding: ItemSignLanguageVideoBinding,
        private val onItemClick: (SignLanguageItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(signLanguageItem: SignLanguageItem) {
            binding.signLanguageVideoItem = signLanguageItem
            binding.root.setOnClickListener { onItemClick(signLanguageItem) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SignLanguageItem>() {
            override fun areItemsTheSame(oldItem: SignLanguageItem, newItem: SignLanguageItem): Boolean {
                return oldItem.videoUri == newItem.videoUri
            }

            override fun areContentsTheSame(oldItem: SignLanguageItem, newItem: SignLanguageItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
