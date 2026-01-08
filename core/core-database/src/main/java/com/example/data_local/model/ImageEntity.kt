package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "designs")
data class ImageEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val imageUrl: String,
    val orderIndex: Int
)
