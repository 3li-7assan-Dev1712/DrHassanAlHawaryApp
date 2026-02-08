package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = false)
    val telegramId: Long,
    val name: String,
    val username: String,
    val photoUrl: String,
    val isChannelMember: Boolean,
    val membershipState: String,
    val isConnectedToTelegram: Boolean
)