package com.example.domain.module

data class QuizSubmissionResult(
    val score: Int,
    val total: Int,
    val passed: Boolean,
    val newLevelId: String?
)
