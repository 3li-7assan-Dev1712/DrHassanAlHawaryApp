package com.example.hassanalhawary.ui.screens.home_screen

import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.model.Audio

data class HomeScreenUiState(


    val loadingQOTDay: Boolean = true,
    val loadingLatestArticles: Boolean = true,
    val loadingLatestAudios: Boolean = true,
    val qotd: String? = null, // the question of the day
    val latestArticles: List<Article> = emptyList(),
    val latestAudios: List<Audio> = emptyList(),
    val searchQuery: String = "",


    )
