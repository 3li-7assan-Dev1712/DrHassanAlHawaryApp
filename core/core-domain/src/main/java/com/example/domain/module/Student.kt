package com.example.domain.module

data class Student(
    val telegramId: Long,
    val name: String,
    val username: String,
    val photoUrl: String,
    val isCourseMember: Boolean,
    val membershipState: String,
    val isConnectedToTelegram: Boolean,
    val currentLevelId: String = "level_1", // Default starting level
    val completedLessonIds: List<String> = emptyList(), // To track progress
    val batch: String? = null
)
