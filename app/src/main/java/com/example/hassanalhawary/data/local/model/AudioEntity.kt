package com.example.hassanalhawary.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hassanalhawary.domain.model.Audio



@Entity(tableName = "audios")
data class AudioEntity(


    @PrimaryKey(autoGenerate = false)
    val audioUrl: String,
    val title: String

)

/**
 * Convert AudioEntity to Audio domain model
 */
fun AudioEntity.toDomainModel(): Audio =
    Audio(
        title = this.title,
        audioUrl = this.audioUrl
    )

