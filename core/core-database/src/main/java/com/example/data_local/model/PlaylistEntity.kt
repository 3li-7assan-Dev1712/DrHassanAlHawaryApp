package com.example.data_local.model

import androidx.room.Entity

@Entity(tableName = "playlists")
data class PlaylistEntity(

    val id: String,
    val title: String,
    val level: Int,
    val thumbnailUrl: String,
)