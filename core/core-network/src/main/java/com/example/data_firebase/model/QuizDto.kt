package com.example.data_firebase.model

import com.example.domain.module.QuizType
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class QuestionTypeDto {
    MCQ, TF
}

data class QuestionDto(
    val id: String = "",
    val text: String = "",
    val type: QuestionTypeDto = QuestionTypeDto.MCQ,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int? = null,
    val correctBooleanAnswer: Boolean? = null
)

data class QuizDto(
    val id: String = "",
    val title: String = "",
    val targetLevelId: String? = null,
    val type: QuizType = QuizType.WEEKLY,
    val questions: List<QuestionDto> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)
