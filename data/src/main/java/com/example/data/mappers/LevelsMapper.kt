package com.example.data.mappers

import com.example.data_firebase.model.LevelDto
import com.example.data_local.model.LevelEntity
import com.example.domain.module.Level

fun LevelDto.toEntity(): LevelEntity {
    return LevelEntity(
        id = id,
        title = title,
        order = order,
    )
}

fun LevelEntity.toDomain(): Level {
    return Level(
        id = id,
        title = title,
        order = order,
        isLocked = false
    )
}