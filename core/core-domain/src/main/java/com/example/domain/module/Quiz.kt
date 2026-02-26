package com.example.domain.module

import java.util.Date

data class Quiz(
    val id: String = "",
    val title: String = "",
    val questions: List<Question> = emptyList(),
    val createdAt: Date? = null
)