package com.example.feature.article.presentation.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.seriesScreenState
import com.example.domain.module.Article
import com.example.domain.use_cases.series.GetSeriesArticlesUseCase
import com.example.domain.use_cases.series.GetSeriesByIdUseCase
import com.example.domain.use_cases.series.RefreshSeriesItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** A series of articles: the cached ones at once, then the server's as they arrive. */
@HiltViewModel
class ArticleSeriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSeriesByIdUseCase: GetSeriesByIdUseCase,
    getSeriesArticlesUseCase: GetSeriesArticlesUseCase,
    refreshSeriesItemsUseCase: RefreshSeriesItemsUseCase,
) : ViewModel() {

    private val seriesId: String = checkNotNull(savedStateHandle["seriesId"])

    val state: StateFlow<SeriesScreenState<Article>> = viewModelScope.seriesScreenState(
        series = getSeriesByIdUseCase(seriesId),
        items = getSeriesArticlesUseCase(seriesId),
        refreshItems = { refreshSeriesItemsUseCase(it) },
    )
}
