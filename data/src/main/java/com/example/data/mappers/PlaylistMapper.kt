package com.example.data.mappers

import com.example.data_firebase.model.PlaylistDto
import com.example.data_local.model.PlaylistEntity
import com.example.domain.module.Playlist
import java.util.Date

fun PlaylistDto.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    publishDate = publishDate,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)

fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    updatedAt = Date(updatedAt)
)

fun Playlist.toDto(): PlaylistDto = PlaylistDto(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    levelId = levelId,
    order = order,
    thumbnailUrl = thumbnailUrl,
    updatedAt = Date(updatedAt)
)
