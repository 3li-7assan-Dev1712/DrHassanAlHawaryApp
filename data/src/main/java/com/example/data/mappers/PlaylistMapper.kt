package com.example.data.mappers

import com.example.data_firebase.model.PlaylistDto
import com.example.data_local.model.PlaylistEntity
import com.example.domain.module.Playlist

fun PlaylistDto.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    title = title,
    levelId = levelId,
    thumbnailUrl = thumbnailUrl,
    order = order,
    updatedAt = updatedAt.time
)


fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    levelId = levelId,
    thumbnailUrl = thumbnailUrl,
    order = order,
    updatedAt = updatedAt
)


fun Playlist.toDto(): PlaylistDto = PlaylistDto(
    id = id,
    title = title,
    levelId = levelId,
    thumbnailUrl = thumbnailUrl,
    order = order,
    updatedAt = updatedAt
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl
)

