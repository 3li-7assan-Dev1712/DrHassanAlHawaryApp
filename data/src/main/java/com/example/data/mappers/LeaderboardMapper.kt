package com.example.data.mappers

import com.example.data_firebase.model.LeaderboardDto
import com.example.domain.module.LeaderBoard
import java.util.Date

fun LeaderboardDto.toDomain(): LeaderBoard {
    return LeaderBoard(
        telegramId = telegramId,
        studentName = studentName,
        telegramPhotoUrl = telegramPhotoUrl,
        score = score,
        answerTimestamp = answerTimestamp ?: Date()
    )
}

fun LeaderBoard.toDto(): LeaderboardDto {
    return LeaderboardDto(
        telegramId = telegramId,
        studentName = studentName,
        telegramPhotoUrl = telegramPhotoUrl,
        score = score,
        answerTimestamp = answerTimestamp
    )
}
