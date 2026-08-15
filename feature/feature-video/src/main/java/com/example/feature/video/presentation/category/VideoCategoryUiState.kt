package com.example.feature.video.presentation.category

import com.example.domain.module.ContentCategory

data class VideoCategoryUiState(
    val categories: List<ContentCategory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
