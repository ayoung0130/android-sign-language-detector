package com.example.sign_language_detector.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sign_language_detector.ui.questions.QuestionsAdapter

@BindingAdapter("questions")
fun setQuestions(recyclerView: RecyclerView, questions: List<String>?) {
    questions?.let {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = QuestionsAdapter(it)
    }
}
