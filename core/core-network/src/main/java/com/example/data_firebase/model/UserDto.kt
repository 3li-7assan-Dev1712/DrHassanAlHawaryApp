package com.example.data_firebase.model


data class UserDto(
    val uid: String? = null,
    val email: String? = null,
    val profile: ProfileDto? = null,
    val telegram: TelegramDto? = null,
    val progress: ProgressDto? = null
)

data class ProfileDto(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val provider: String? = null
)

data class TelegramDto(
    val id: Long? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val photoUrl: String? = null,
    val isChannelMember: Boolean? = null,
    val membershipState: String? = null,
    val isConnectedToTelegram: Boolean? = null
)

data class ProgressDto(
    val currentLevelId: String? = null
)