package com.ayeong.sign_language_detector.ui.questions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
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

            binding.questionCard.setOnClickListener {
                val navController = binding.root.findNavController()

                // SafeArgs로 nullable 인수를 넘기는 메서드 사용
                val action = QuestionsFragmentDirections.actionQuestionsToCamera(question)
                navController.navigate(action)
            }
        }
    }
}
