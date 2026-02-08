package com.example.domain.module

data class Student(
    val telegramId: Long,
    val name: String,
    val username: String,
    val photoUrl: String,
    val isCourseMember: Boolean,
    val membershipState: String,
    val isConnectedToTelegram: Boolean
)