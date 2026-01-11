package com.example.feature.audio.data.mapper

import com.example.data_firebase.model.AudioDto
import com.example.data_local.model.AudioEntity
import com.example.feature.audio.domain.model.Audio
import java.util.Date


fun AudioDto.toDomain(
): Audio = Audio(
    id = this.id,
    title = this.title,
    audioUrl = this.audioUrl,
    durationInMillis = this.durationInMillis,
    publishDate = Date(this.publishDate),
    isFavorite = false,
    isDownloaded = false,
    lastPlayedTimestamp = null,
    isPlaying = false

)

fun AudioDto.toEntity(
    isFavorite: Boolean,
    isDownloaded: Boolean,
    lastPlayedTimestamp: Long?,
    localFilePath: String?
): AudioEntity = AudioEntity(
    id = this.id,
    title = this.title,
    audioUrl = this.audioUrl,
    durationInMillis = this.durationInMillis,
    publishDate = this.publishDate,
    isFavorite = isFavorite,
    isDownloaded = isDownloaded,
    lastPlayedTimestamp = lastPlayedTimestamp,
    localFilePath = localFilePath,
)

fun AudioEntity.toDomain(
    isFavorite: Boolean,
    isDownloaded: Boolean,
    lastPlayedTimestamp: Long?,
    localFilePath: String?
): Audio = Audio(
    id = this.id,
    title = this.title,
    audioUrl = this.audioUrl,
    durationInMillis = this.durationInMillis,
    publishDate = Date(this.publishDate),
    isFavorite = isFavorite,
    isDownloaded = isDownloaded,
    lastPlayedTimestamp = lastPlayedTimestamp,
    localFilePath = localFilePath,
    isPlaying = false

)
