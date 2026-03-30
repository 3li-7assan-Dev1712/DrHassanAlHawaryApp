package com.example.data.mappers

import com.example.data_firebase.model.LessonDto
import com.example.data_local.model.LessonEntity
import com.example.domain.module.Lesson
import java.util.Locale
import java.util.concurrent.TimeUnit

fun LessonDto.toEntity(): LessonEntity = LessonEntity(
    id = id,
    title = title,
    order = order,
    playlistId = playlistId,
    audioRemoteUrl = audioUrl,
    audioFilePath = null,
    duration = duration,
    pdfRemoteUrl = pdfUrl,
    pdfFilePath = null,
    publishDate = publishDate,
    updatedAt = updatedAt
)

fun LessonDto.toDomain(): Lesson = Lesson(
    id = id,
    title = title,
    order = order,
    audioUrl = audioUrl,
    pdfUrl = pdfUrl,
    duration = formatDuration(duration),
)

fun Lesson.toDto(): LessonDto =
    LessonDto(
    id = id,
    title = title,
    order = order,
    audioUrl = audioUrl,
    pdfUrl = pdfUrl,
)

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    title = title,
    order = order,
    audioUrl = audioFilePath ?: audioRemoteUrl,
    pdfUrl = pdfFilePath ?: pdfRemoteUrl,
    duration = formatDuration(duration)
)

fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
