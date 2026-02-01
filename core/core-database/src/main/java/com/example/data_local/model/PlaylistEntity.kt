package com.example.data_local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity( tableName = "playlists",
    foreignKeys = [
        ForeignKey(
            entity = LevelEntity::class,
            parentColumns = ["id"],
            childColumns = ["levelId"]
        )
    ],
    indices = [Index("levelId")]
)
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val levelId: String,
    val order: Int,
    val thumbnailUrl: String,
    val updatedAt: Long
)