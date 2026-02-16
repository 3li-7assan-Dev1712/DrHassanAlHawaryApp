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
    updatedAt = updatedAt.time
)

fun LessonDto.toDomain(): Lesson = Lesson(
    id = id,
    title = title,
    audioUrl = audioUrl,
    pdfUrl = pdfUrl,
    duration = formatDuration(duration),
)

fun Lesson.toDto(
    playlistId: String,
    order: Int
): LessonDto = LessonDto(
    id = id,
    title = title,
    order = order,
    playlistId = playlistId
)

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    title = title,
    audioUrl = audioFilePath ?: audioRemoteUrl,
    pdfUrl = pdfFilePath ?: pdfRemoteUrl,
    duration = formatDuration(duration)

)

fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)

    return if (hours > 0) {
        // Include hours if the duration is an hour or longer
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        // Otherwise, just show minutes and seconds
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
