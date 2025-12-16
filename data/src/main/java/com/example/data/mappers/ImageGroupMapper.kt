package com.example.data.mappers

import com.example.data_local.model.ImageGroupEntity
import com.example.domain.module.ImageGroup

fun ImageGroup.toEntity(): ImageGroupEntity {
    return ImageGroupEntity(
        id = this.id,
        title = this.title,
        publishDate = this.publishDate.time,
        previewImageUrl = this.previewImageUrl
    )
}