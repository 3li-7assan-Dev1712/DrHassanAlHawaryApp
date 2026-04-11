package com.example.domain.module

import java.util.Date





data class Quiz(
    val id: String = "",
    val title: String = "",
    val questions: List<Question> = emptyList(),
    val createdAt: Date? = null,
    val type: QuizType = QuizType.WEEKLY,
    val targetLevelId: String? = null, // The level this exam unlocks if passed
    val batchIds: List<String> = emptyList(),
    val isActive: Boolean = true,
    val startAt: Date? = null,
    val endAt: Date? = null,
)
