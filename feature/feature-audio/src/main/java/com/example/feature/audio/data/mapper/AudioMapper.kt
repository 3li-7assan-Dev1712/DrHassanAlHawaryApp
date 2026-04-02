package com.example.feature.audio.data.mapper

import com.example.data_local.model.AudioEntity
import com.example.feature.audio.domain.model.Audio
import java.util.Date


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
    isPlaying = false,
    type = this.type

)
