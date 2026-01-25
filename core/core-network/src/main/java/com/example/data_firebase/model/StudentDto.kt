package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties


@Keep // Prevents Proguard/R8 from removing this class during release builds, which would break Firebase mapping.
@IgnoreExtraProperties
data class StudentDto(
    val telegramId: Long = 0,
    val firstName: String = "",
    val secondName: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val isChannelMember: Boolean = false,
    val isConnectedToTelegram: Boolean = false
)