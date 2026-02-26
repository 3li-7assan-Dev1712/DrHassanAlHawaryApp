package com.example.data_firebase.model

import java.util.Date

data class LeaderboardDto(
    val studentName: String = "",
    val telegramPhotoUrl: String = "",
    val score: Int = 0,
    val answerTimestamp: Date? = null
)
