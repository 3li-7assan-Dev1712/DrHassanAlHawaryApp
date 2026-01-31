package com.example.data_local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val order: Int,
)