package com.example.hassanalhawary.ui.screens.home_screen

import com.example.hassanalhawary.domain.model.Article
import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.Wisdom
import com.example.hassanalhawary.domain.model.WisdomResult
import com.example.hassanalhawary.domain.model.fakeWisdom

data class HomeScreenUiState(


    val loadingWotd: Boolean = true, // loading the wisdom of the day (is loading show progress otherwise hide it)
    val loadingLatestArticles: Boolean = true,
    val loadingLatestAudios: Boolean = true,
    val wotdResult: WisdomResult<Wisdom, String> = WisdomResult.Success(fakeWisdom), // the wisdom of the day
    val latestArticles: List<Article> = emptyList(),
    val latestAudios: List<Audio> = emptyList(),
    val searchQuery: String = "",
    val isInOfflineMode: Boolean = false,


    val audioErrorMessage: String? = null,
    )
