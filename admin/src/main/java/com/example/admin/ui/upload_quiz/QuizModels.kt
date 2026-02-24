package com.example.admin.ui.upload_quiz

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class QuestionType {
    MCQ, TF
}

data class Question(
    val id: String = "",
    val text: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int? = null, // For MCQ
    val correctBooleanAnswer: Boolean? = null // For TF
)

data class Quiz(
    val id: String = "",
    val title: String = "",
    val questions: List<Question> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)
