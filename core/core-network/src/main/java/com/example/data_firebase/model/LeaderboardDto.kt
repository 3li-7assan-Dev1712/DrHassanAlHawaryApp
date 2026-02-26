package com.example.data_firebase.model

import java.util.Date

data class LeaderboardDto(
    val telegramId: Long = 0,
    val studentName: String = "",
    val telegramPhotoUrl: String = "",
    val score: Int = 0,
    val answerTimestamp: Date? = null
)
