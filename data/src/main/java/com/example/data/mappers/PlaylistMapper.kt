package com.example.data.mappers

import com.example.data_firebase.model.PlaylistDto
import com.example.data_local.model.PlaylistEntity
import com.example.domain.module.Playlist
import com.google.firebase.Timestamp
import java.util.Date

fun PlaylistDto.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    publishDate = publishDate?.toDate()?.time ?: 0L,
    updatedAt = updatedAt?.toDate()?.time ?: 0L
)

fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    updatedAt = updatedAt?.toDate() ?: Date()
)

fun Playlist.toDto(): PlaylistDto = PlaylistDto(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    updatedAt = Timestamp(updatedAt),
    isDeleted = false
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    updatedAt = Date(updatedAt)
)
