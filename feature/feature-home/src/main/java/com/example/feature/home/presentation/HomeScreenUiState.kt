package com.example.feature.home.presentation

import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.domain.model.ImageFeed

data class HomeScreenUiState(


    val loadingWotd: Boolean = true, // loading the wisdom of the day (is loading show progress otherwise hide it)
    val loadingLatestArticles: Boolean = true,
    val loadingLatestAudios: Boolean = true,
    val latestImages: List<ImageFeed> = emptyList(),
    val latestArticles: List<ArticleFeed> = emptyList(),
    val latestAudios: List<AudioFeed> = emptyList(),
    val searchQuery: String = "",
    val isInOfflineMode: Boolean = false,


    val audioErrorMessage: String? = null,
)
