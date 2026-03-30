package com.example.data.mappers

import com.example.data_firebase.model.ImageGroupDto
import com.example.data_local.model.ImageGroupEntity
import com.example.domain.module.ImageGroup
import java.util.Date

fun ImageGroup.toEntity(updatedAt: Long = System.currentTimeMillis(), isDeleted: Boolean = false): ImageGroupEntity {
    return ImageGroupEntity(
        id = this.id,
        title = this.title,
        publishDate = this.publishDate.time,
        previewImageUrl = this.previewImageUrl,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}

fun ImageGroupEntity.toDomainModel(): ImageGroup {
    return ImageGroup(
        id = this.id,
        title = this.title,
        publishDate = Date(this.publishDate),
        previewImageUrl = this.previewImageUrl
    )
}

fun ImageGroupDto.toEntity(): ImageGroupEntity =
    ImageGroupEntity(
        id = id,
        title = title,
        publishDate = publishDate?.toDate()?.time ?: 0L,
        updatedAt = updatedAt?.toDate()?.time ?: 0L,
        isDeleted = isDeleted,
        previewImageUrl = previewImageUrl
    )
