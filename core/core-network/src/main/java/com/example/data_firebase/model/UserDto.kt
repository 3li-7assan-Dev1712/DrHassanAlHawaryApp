package com.example.data_firebase.model

data class UserDto(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val telegramId: Long? = null,
    val telegramUsername: String? = null,
    val telegramFirstName: String? = null,
    val telegramLastName: String? = null,
    val telegramPhotoUrl: String? = null,
    val membershipState: String? = null,
    val isChannelMember: Boolean = false,
    val isConnectedToTelegram: Boolean = false,
    val currentLevelId: String = "level_1"
)
