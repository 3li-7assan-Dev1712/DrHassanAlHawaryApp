package com.example.feature.article.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Article
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.feature.article.domain.use_case.GetPaginatedArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class ArticleListViewModel @Inject constructor(

    getPaginatedArticlesUseCase: GetPaginatedArticlesUseCase,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<PagingData<Article>> =
        getPaginatedArticlesUseCase("").cachedIn(viewModelScope)

    /** The article series, shown above the standalone articles. */
    val series: StateFlow<List<Series>> = getSeriesUseCase(SeriesCollection.ARTICLES)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Network failures are already logged; a cache write failing leaves the cached series.
        viewModelScope.launch { runCatching { syncSeriesUseCase() } }
    }


}
