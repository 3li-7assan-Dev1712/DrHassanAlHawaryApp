package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A push notification the app received, kept so the notifications screen can list it. */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val receivedAt: Long,
    val isRead: Boolean = false,
)
