package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val telegramId: Long?,
    val telegramUsername: String?,
    val telegramFirstName: String?,
    val telegramLastName: String?,
    val telegramPhotoUrl: String?,
    val membershipState: String?,
    val isChannelMember: Boolean,
    val isConnectedToTelegram: Boolean,
    val currentLevelId: String = "level_1"
)
