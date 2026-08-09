package com.example.feature.audio.presentation.category

import com.example.domain.module.ContentCategory

data class AudioCategoryUiState(
    val categories: List<ContentCategory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
