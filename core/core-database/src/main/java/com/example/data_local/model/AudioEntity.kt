package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "audios")
data class AudioEntity(


    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val audioUrl: String,
    val durationInMillis: Long,
    val publishDate: Long,
    val isDownloaded: Boolean = false,
    //User-Specific
    val isFavorite: Boolean = false,
    val localFilePath: String? = null,
    // Never written: where an audio was left lives in playback_progress (PlaybackProgressEntity).
    val lastPlayedTimestamp: Long? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val type: String = "",
    val categoryId: String? = null,
    /** The series this item belongs to (null: a standalone item). */
    val seriesId: String? = null,
    /** 1-based position inside [seriesId]. */
    val episodeNumber: Long? = null,
)

