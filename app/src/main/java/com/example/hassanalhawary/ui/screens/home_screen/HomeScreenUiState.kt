package com.example.hassanalhawary.ui.screens.home_screen

import com.example.domain.module.Article
import com.example.domain.module.Audio
import com.example.domain.module.Wisdom
import com.example.domain.module.WisdomResult
import com.example.domain.module.fakeWisdom
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
