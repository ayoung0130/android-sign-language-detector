package com.ayeong.sign_language_detector.ui.questions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ayeong.sign_language_detector.R
import com.ayeong.sign_language_detector.databinding.ItemQuestionBinding

class QuestionsAdapter(private val questions: List<String>) :
    RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemQuestionBinding.inflate(inflater, parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount(): Int = questions.size

    class QuestionViewHolder(private val binding: ItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: String) {
            binding.question = question
            binding.executePendingBindings()

            binding.questionText.setOnClickListener {
                // NavController를 사용하여 CameraFragment로 이동
                val navController = binding.root.findNavController()
                navController.navigate(R.id.action_questions_to_camera)
            }
        }
    }
}
