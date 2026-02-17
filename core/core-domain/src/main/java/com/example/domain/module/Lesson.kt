package com.example.domain.module

data class Lesson(
    val id: String,
    val title: String,
    val order: Int = 0,
    val audioUrl: String,
    val pdfUrl: String,
    val duration: String,
)