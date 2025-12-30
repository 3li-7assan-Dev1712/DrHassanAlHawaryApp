package com.example.data.mappers

import com.example.data_local.model.VideoEntity
import com.example.domain.module.Video
import java.util.Date

fun VideoEntity.toDomainModel(): Video =
    Video(
        id = this.id,
        title = this.title,
        videoUrl = this.videoUrl,
        publishDate = Date(this.publishDate),
        youtubeVideoId = this.youtubeVideoId
    )

/**
 * Convert Audio domain to model AudioEntity
 */
fun Video.toEntity(): VideoEntity =
    VideoEntity(
        id = this.id,
        title = this.title,
        videoUrl = this.videoUrl,
        publishDate = this.publishDate.time,
        youtubeVideoId = this.youtubeVideoId
    )
