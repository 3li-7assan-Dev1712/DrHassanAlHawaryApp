package com.example.domain.module

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val telegramId: Long?,
    val telegramUsername: String?,
    val telegramFirstName: String?,
    val telegramLastName: String?,
    val telegramPhotoUrl: String?,
    val isChannelMember: Boolean,
    val membershipState: String?,
    val isConnectedToTelegram: Boolean,
    val currentLevelId: String = "level_1"
)
