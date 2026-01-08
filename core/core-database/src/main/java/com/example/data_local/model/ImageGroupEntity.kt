package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.module.ImageGroup
import java.util.Date

@Entity(tableName = "image_groups")
data class ImageGroupEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val publishDate: Long,
    val previewImageUrl: String
)

/**
 * A handy extension function to map a database entity to a domain model.
 * This keeps the domain layer clean of any database-specific annotations.
 */
fun ImageGroupEntity.toDomainModel(): ImageGroup {
    return ImageGroup(
        id = this.id,
        title = this.title,
        publishDate = Date(this.publishDate),
        previewImageUrl = this.previewImageUrl
    )
}