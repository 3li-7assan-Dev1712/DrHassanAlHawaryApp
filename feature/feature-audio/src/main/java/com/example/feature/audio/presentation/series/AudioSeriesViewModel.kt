package com.example.feature.audio.presentation.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.seriesScreenState
import com.example.domain.module.Audio
import com.example.domain.use_cases.series.GetSeriesAudiosUseCase
import com.example.domain.use_cases.series.GetSeriesByIdUseCase
import com.example.domain.use_cases.series.RefreshSeriesItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** A series of audios: the cached lessons at once, then the server's as they arrive. */
@HiltViewModel
class AudioSeriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSeriesByIdUseCase: GetSeriesByIdUseCase,
    getSeriesAudiosUseCase: GetSeriesAudiosUseCase,
    refreshSeriesItemsUseCase: RefreshSeriesItemsUseCase,
) : ViewModel() {

    private val seriesId: String = checkNotNull(savedStateHandle["seriesId"])

    val state: StateFlow<SeriesScreenState<Audio>> = viewModelScope.seriesScreenState(
        series = getSeriesByIdUseCase(seriesId),
        items = getSeriesAudiosUseCase(seriesId),
        refreshItems = { refreshSeriesItemsUseCase(it) },
    )
}
