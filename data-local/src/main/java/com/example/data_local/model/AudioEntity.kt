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
    //User-Specific
    val isFavorite: Boolean = false,
    val localFilePath: String? = null,
    val lastPlayedTimestamp: Long? = null // will be used to play from the last position
)


