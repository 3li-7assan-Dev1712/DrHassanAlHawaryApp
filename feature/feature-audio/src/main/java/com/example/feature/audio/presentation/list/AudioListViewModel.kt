package com.example.feature.audio.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Audio
import com.example.domain.module.PlaybackProgress
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.progress.ObservePlaybackProgressUseCase
import com.example.domain.use_cases.progress.ObserveSeriesCompletionUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.feature.audio.domain.use_case.GetPaginatedAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class AudioListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPaginatedAudioUseCase: GetPaginatedAudioUseCase,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
    observePlaybackProgressUseCase: ObservePlaybackProgressUseCase,
    observeSeriesCompletionUseCase: ObserveSeriesCompletionUseCase,
) : ViewModel() {


    private val categoryId: String? = savedStateHandle["categoryId"]
    val categoryTitle: String? = savedStateHandle["categoryTitle"]

    private val _uiState = MutableStateFlow<AudioListUiState>(AudioListUiState.Loading())

    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val audios: Flow<PagingData<Audio>> = getPaginatedAudioUseCase("", categoryId).cachedIn(viewModelScope)

    /** The series, shown as cards with «متفرقات» when there are any; null until read. */
    val series: StateFlow<List<Series>?> = getSeriesUseCase(SeriesCollection.AUDIOS, categoryId)
        .stateIn<List<Series>?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Where each audio was left, by URL: the rows' progress. */
    val progress: StateFlow<Map<String, PlaybackProgress>> = observePlaybackProgressUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** The lessons finished in each series the listener started, by series id: their cards. */
    val seriesCompletion: StateFlow<Map<String, Int>> = observeSeriesCompletionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    init {
        // Network failures are already logged; a cache write failing leaves the cached series.
        viewModelScope.launch { runCatching { syncSeriesUseCase() } }
    }
}
