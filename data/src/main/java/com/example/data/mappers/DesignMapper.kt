package com.example.data.mappers

import com.example.data_local.model.ImageEntity
import com.example.domain.module.Image


/**
 * Convert ArticleAudio to Article domain model
 */
fun ImageEntity.toDomainModel(): Image =
    Image(
        id = this.id,
        imageUrl = this.imageUrl,
        orderIndex = this.orderIndex,
    )