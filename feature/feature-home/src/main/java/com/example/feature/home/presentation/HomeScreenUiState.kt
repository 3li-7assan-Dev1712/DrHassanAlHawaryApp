package com.example.feature.home.presentation

import com.example.domain.module.PlaybackProgress
import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.domain.model.ImageFeed

data class HomeScreenUiState(


    val loadingImages: Boolean = true,
    val loadingLatestArticles: Boolean = true,
    val loadingLatestAudios: Boolean = true,
    val latestImages: List<ImageFeed> = emptyList(),
    val latestArticles: List<ArticleFeed> = emptyList(),
    val latestAudios: List<AudioFeed> = emptyList(),
    /** «متابعة الاستماع»: the audios left part way, the latest first (none: no section). */
    val continueListening: List<PlaybackProgress> = emptyList(),
    val searchQuery: String = "",
    val isInOfflineMode: Boolean = false,
    val errorMessage: String? = null,
    val unreadNotifications: Int = 0,
)
