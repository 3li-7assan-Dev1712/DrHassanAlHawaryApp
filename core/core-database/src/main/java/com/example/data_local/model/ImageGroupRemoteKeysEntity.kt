package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "image_group_remote_keys")
data class ImageGroupRemoteKeysEntity(
    @PrimaryKey
    val groupId: String,
    val nextKey: String?
)