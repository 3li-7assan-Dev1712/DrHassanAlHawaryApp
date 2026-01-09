package com.example.data_local.model

import androidx.room.Embedded
import androidx.room.Relation

/*
this is a pojo class (plain old java object)
to represent the relationship between ImageGroupEntity and ImageEntity
 */
data class ImageGroupWithImages(
    @Embedded
    val group: ImageGroupEntity,

    @Relation(
        parentColumn = "id", // From ImageGroupEntity
        entityColumn = "group_id" // From ImageEntity
    )
    val images: List<ImageEntity>
)