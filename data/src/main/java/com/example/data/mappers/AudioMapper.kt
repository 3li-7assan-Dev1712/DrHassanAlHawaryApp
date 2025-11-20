package com.example.data.mappers

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
        isDownloaded = this.localFilePath != null,
        localFilePath = this.localFilePath,
        lastPlayedTimestamp = this.lastPlayedTimestamp
    )

/**
 * Convert Audio domain to model AudioEntity
 */
fun Audio.toEntity(): AudioEntity =
    AudioEntity(
        id = this.id,
        title = this.title,
        audioUrl = this.audioUrl,
        durationInMillis = this.durationInMillis,
        publishDate = this.publishDate.time,
        isFavorite = this.isFavorite,
        localFilePath = this.localFilePath,
        lastPlayedTimestamp = this.lastPlayedTimestamp
    )