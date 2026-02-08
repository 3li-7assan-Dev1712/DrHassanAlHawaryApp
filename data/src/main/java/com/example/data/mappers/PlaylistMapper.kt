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

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl
)

