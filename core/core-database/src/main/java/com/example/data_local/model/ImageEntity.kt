package com.example.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.module.Image


@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "group_id") val groupId: String,
    val imageUrl: String,
    val orderIndex: Int
) {
    fun toDomainModel(): Image {
        return Image(
            id = this.id,
            imageUrl = this.imageUrl,
            orderIndex = this.orderIndex
        )
    }
}
