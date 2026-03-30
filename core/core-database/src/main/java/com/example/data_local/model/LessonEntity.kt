package com.example.data_local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity( tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val order: Int,
    val title: String,
    val audioRemoteUrl: String,
    val audioFilePath: String?,
    val duration: Long,
    val pdfRemoteUrl: String,
    val pdfFilePath: String?,
    val publishDate: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
