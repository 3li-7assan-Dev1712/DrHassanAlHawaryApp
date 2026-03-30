package com.example.data.mappers

import com.example.data_firebase.model.VideoDto
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

fun Video.toEntity(updatedAt: Long = System.currentTimeMillis()): VideoEntity =
    VideoEntity(
        id = this.id,
        title = this.title,
        videoUrl = this.videoUrl,
        publishDate = this.publishDate.time,
        youtubeVideoId = this.youtubeVideoId,
        updatedAt = updatedAt
    )

fun VideoDto.toDomainModel(): Video = Video(
    id = this.id,
    title = this.title,
    videoUrl = this.videoUrl,
    publishDate = this.publishDate?.toDate() ?: Date(),
    youtubeVideoId = this.videoYoutubeId
)

fun VideoDto.toEntity(): VideoEntity =
    VideoEntity(
        id = id,
        title = title,
        videoUrl = videoUrl,
        publishDate = publishDate?.toDate()?.time ?: 0L,
        youtubeVideoId = videoYoutubeId,
        updatedAt = updatedAt?.toDate()?.time ?: 0L
    )
