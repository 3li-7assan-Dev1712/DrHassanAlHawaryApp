package com.example.domain.module

data class Level(
    val id: String,
    val title: String,
    val order: Int,
    val isLocked: Boolean
)