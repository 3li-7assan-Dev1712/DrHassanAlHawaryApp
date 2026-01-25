package com.example.study.domain.model

data class Student(
    val telegramId: Long,
    val name: String,
    val username: String,
    val photoUrl: String,
    val isCourseMember: Boolean,
    val isConnectedToTelegram: Boolean
)