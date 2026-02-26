package com.example.domain.module

data class Question(
    val id: String = "",
    val text: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int? = null, // For MCQ
    val correctBooleanAnswer: Boolean? = null // For TF
)