package com.example.data.mappers

import com.example.data_local.model.PlaybackProgressEntity
import com.example.data_local.model.SeriesLessonProgressRow
import com.example.domain.module.PlaybackProgress
import com.example.domain.module.SeriesLessonProgress

fun PlaybackProgressEntity.toDomain(): PlaybackProgress =
    PlaybackProgress(
        audioUrl = audioUrl,
        title = title,
        positionMs = positionMs,
        durationMs = durationMs,
        updatedAt = updatedAt,
    )

fun PlaybackProgress.toEntity(): PlaybackProgressEntity =
    PlaybackProgressEntity(
        audioUrl = audioUrl,
        title = title,
        positionMs = positionMs,
        durationMs = durationMs,
        updatedAt = updatedAt,
    )

fun SeriesLessonProgressRow.toDomain(): SeriesLessonProgress =
    SeriesLessonProgress(seriesId = seriesId, progress = progress.toDomain())
