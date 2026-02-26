package com.example.domain.module

import java.util.Date

data class LeaderBoard(
    val studentName: String,
    val telegramPhotoUrl: String,
    val score: Int,
    val answerTimestamp: Date
)