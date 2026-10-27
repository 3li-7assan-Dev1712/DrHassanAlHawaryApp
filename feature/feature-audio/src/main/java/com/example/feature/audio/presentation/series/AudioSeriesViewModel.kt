package com.example.feature.audio.presentation.series

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.seriesScreenState
import com.example.domain.module.Audio
import com.example.domain.module.PlaybackProgress
import com.example.domain.module.SeriesProgress
import com.example.domain.use_cases.audios.DownloadAudioUseCase
import com.example.domain.use_cases.audios.DownloadResult
import com.example.domain.use_cases.progress.ObservePlaybackProgressUseCase
import com.example.domain.use_cases.series.GetSeriesAudiosUseCase
import com.example.domain.use_cases.series.GetSeriesByIdUseCase
import com.example.domain.use_cases.series.RefreshSeriesItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * "Download the series" on its screen: [progress] (0..1, over all its lessons) while it runs,
 * null otherwise; [allSaved] once every lesson is on the device.
 */
data class SeriesDownload(val progress: Float? = null, val allSaved: Boolean = false)

/** A series of audios: the cached lessons at once, then the server's as they arrive. */
@HiltViewModel
class AudioSeriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSeriesByIdUseCase: GetSeriesByIdUseCase,
    getSeriesAudiosUseCase: GetSeriesAudiosUseCase,
    refreshSeriesItemsUseCase: RefreshSeriesItemsUseCase,
    private val downloadAudioUseCase: DownloadAudioUseCase,
    observePlaybackProgressUseCase: ObservePlaybackProgressUseCase,
) : ViewModel() {

    private val seriesId: String = checkNotNull(savedStateHandle["seriesId"])

    val state: StateFlow<SeriesScreenState<Audio>> = viewModelScope.seriesScreenState(
        series = getSeriesByIdUseCase(seriesId),
        items = getSeriesAudiosUseCase(seriesId),
        refreshItems = { refreshSeriesItemsUseCase(it) },
    )

    /** Where each lesson was left, by URL: the rows' progress. */
    val progress: StateFlow<Map<String, PlaybackProgress>> = observePlaybackProgressUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** The lessons finished and the one «متابعة» opens, for the header. */
    val seriesProgress: StateFlow<SeriesProgress> = combine(state, progress) { shown, heard ->
        SeriesProgress.of(shown.items.map { it.audioUrl }, heard)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeriesProgress.NotStarted)

    private val downloadProgress = MutableStateFlow<Float?>(null)
    private var downloadJob: Job? = null

    val download: StateFlow<SeriesDownload> = combine(state, downloadProgress) { shown, progress ->
        SeriesDownload(progress, allSaved = shown.items.isNotEmpty() && shown.items.all { it.isDownloaded })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeriesDownload())

    /**
     * Saves the lessons not on the device yet, one after another, with the player's own
     * download (each lesson then shows "محفوظ"); tapped again while it runs, stops it. It runs
     * while the series' screen is open. A failed lesson (offline, no space) stops the rest;
     * the lessons already saved stay.
     */
    fun onDownloadSeries() {
        if (downloadJob?.isActive == true) {
            downloadJob?.cancel()
            return
        }
        val lessons = state.value.items
        val pending = lessons.filterNot { it.isDownloaded }
        if (pending.isEmpty()) return
        downloadJob = viewModelScope.launch {
            try {
                for ((index, audio) in pending.withIndex()) {
                    val done = (lessons.size - pending.size + index).toFloat()
                    downloadProgress.value = done / lessons.size
                    var failed = false
                    downloadAudioUseCase(audio).collect { result ->
                        when (result) {
                            is DownloadResult.Progress ->
                                downloadProgress.value = (done + result.percentage.coerceIn(0, 100) / 100f) / lessons.size
                            is DownloadResult.Success -> Unit
                            is DownloadResult.Error -> {
                                Log.e(TAG, "Series download stopped at ${audio.id}: ${result.message}")
                                failed = true
                            }
                        }
                    }
                    if (failed) break
                }
            } finally {
                downloadProgress.value = null
            }
        }
    }

    private companion object {
        const val TAG = "AudioSeriesViewModel"
    }
}
