package com.example.data.mappers

import com.example.data_firebase.model.AudioDto
import com.example.data_local.model.AudioEntity
import com.example.domain.module.Audio
import java.util.Date


/**
 * Convert AudioEntity to Audio domain model
 */
fun AudioEntity.toDomainModel(): Audio =
    Audio(
        id = this.id,
        title = this.title,
        audioUrl = this.audioUrl,
        durationInMillis = this.durationInMillis,
        publishDate = Date(this.publishDate),
        isFavorite = this.isFavorite,
        isPlaying = false,
        isDownloaded = this.isDownloaded,
        localFilePath = this.localFilePath,
        lastPlayedTimestamp = this.lastPlayedTimestamp
    )

/**
 * Convert Audio domain to model AudioEntity
 */
fun Audio.toEntity(updatedAt: Long = System.currentTimeMillis()): AudioEntity =
    AudioEntity(
        id = this.id,
        title = this.title,
        audioUrl = this.audioUrl,
        durationInMillis = this.durationInMillis,
        publishDate = this.publishDate.time,
        isFavorite = this.isFavorite,
        localFilePath = this.localFilePath,
        lastPlayedTimestamp = this.lastPlayedTimestamp,
        updatedAt = updatedAt,
        isDownloaded = isDownloaded
    )

fun AudioDto.toEntity(): AudioEntity =
    AudioEntity(
        id = id,
        title = title,
        audioUrl = audioUrl,
        durationInMillis = durationInMillis,
        publishDate = publishDate?.toDate()?.time ?: 0L,
        updatedAt = updatedAt?.toDate()?.time ?: 0L
    )

fun AudioDto.toDomainModel(): Audio =
    Audio(
        id = id,
        title = title,
        audioUrl = audioUrl,
        durationInMillis = durationInMillis,
        publishDate = publishDate?.toDate() ?: Date(),
        isFavorite = false,
        isPlaying = false,
        isDownloaded = false,
        localFilePath = null,
        lastPlayedTimestamp = null
    )
