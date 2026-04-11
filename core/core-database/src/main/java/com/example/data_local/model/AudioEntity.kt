package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.module.Audio
import java.util.Date


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
    val lastPlayedTimestamp: Long? = null, // will be used to play from the last position
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val type: String = "",
) {
    fun AudioEntity.toDomain(
        isFavorite: Boolean = this.isFavorite,
        isDownloaded: Boolean = this.isDownloaded,
        lastPlayedTimestamp: Long? = this.lastPlayedTimestamp,
        localFilePath: String? = this.localFilePath,
    ): Audio = Audio(
        id = this.id,
        title = this.title,
        audioUrl = this.audioUrl,
        durationInMillis = this.durationInMillis,
        publishDate = Date(this.publishDate),
        isFavorite = isFavorite,
        isDownloaded = isDownloaded,
        lastPlayedTimestamp = lastPlayedTimestamp,
        localFilePath = localFilePath,
        isPlaying = false,
        type = this.type,
    )
}
