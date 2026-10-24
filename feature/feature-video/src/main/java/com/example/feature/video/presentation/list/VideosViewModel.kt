package com.example.feature.video.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.module.Video
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.feature.video.domain.use_case.GetPaginatedVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    getPaginatedVideoUseCase: GetPaginatedVideoUseCase,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val categoryTitle: String? = savedStateHandle["categoryTitle"]

    /**
     * The category shown (null: all). The route's on the phone; on a tablet the category
     * pills above the grid change it, kept in the saved state across rotation.
     */
    val categoryId: StateFlow<String?> = savedStateHandle.getStateFlow(CATEGORY_ID, null)

    /** The videos outside any series. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val videos: Flow<PagingData<Video>> = categoryId
        .flatMapLatest { getPaginatedVideoUseCase(it) }
        .cachedIn(viewModelScope)

    /** The category's series, as cards above its videos; null until read. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val series: StateFlow<List<Series>?> = categoryId
        .flatMapLatest { getSeriesUseCase(SeriesCollection.VIDEOS, it) }
        .stateIn<List<Series>?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Network failures are already logged; a cache write failing leaves the cached series.
        viewModelScope.launch { runCatching { syncSeriesUseCase() } }
    }

    fun selectCategory(id: String?) {
        savedStateHandle[CATEGORY_ID] = id
    }

    private companion object {
        const val CATEGORY_ID = "categoryId"
    }
}
