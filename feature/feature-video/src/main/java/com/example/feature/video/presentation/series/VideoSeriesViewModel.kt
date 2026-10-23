package com.example.feature.video.presentation.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.seriesScreenState
import com.example.domain.module.Video
import com.example.domain.use_cases.series.GetSeriesByIdUseCase
import com.example.domain.use_cases.series.GetSeriesVideosUseCase
import com.example.domain.use_cases.series.RefreshSeriesItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** A series of videos: the cached episodes at once, then the server's as they arrive. */
@HiltViewModel
class VideoSeriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSeriesByIdUseCase: GetSeriesByIdUseCase,
    getSeriesVideosUseCase: GetSeriesVideosUseCase,
    refreshSeriesItemsUseCase: RefreshSeriesItemsUseCase,
) : ViewModel() {

    private val seriesId: String = checkNotNull(savedStateHandle["seriesId"])

    val state: StateFlow<SeriesScreenState<Video>> = viewModelScope.seriesScreenState(
        series = getSeriesByIdUseCase(seriesId),
        items = getSeriesVideosUseCase(seriesId),
        refreshItems = { refreshSeriesItemsUseCase(it) },
    )
}
