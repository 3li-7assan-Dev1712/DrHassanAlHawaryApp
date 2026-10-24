package com.example.feature.image.presentation.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.seriesScreenState
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup
import com.example.domain.use_cases.series.GetSeriesByIdUseCase
import com.example.domain.use_cases.series.GetSeriesImageGroupsUseCase
import com.example.domain.use_cases.series.RefreshSeriesItemsUseCase
import com.example.feature.image.domain.use_case.GetGroupImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** A series of designs: the cached ones at once, then the server's as they arrive. */
@HiltViewModel
class DesignSeriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSeriesByIdUseCase: GetSeriesByIdUseCase,
    getSeriesImageGroupsUseCase: GetSeriesImageGroupsUseCase,
    refreshSeriesItemsUseCase: RefreshSeriesItemsUseCase,
    private val getGroupImagesUseCase: GetGroupImagesUseCase,
) : ViewModel() {

    private val seriesId: String = checkNotNull(savedStateHandle["seriesId"])

    val state: StateFlow<SeriesScreenState<ImageGroup>> = viewModelScope.seriesScreenState(
        series = getSeriesByIdUseCase(seriesId),
        items = getSeriesImageGroupsUseCase(seriesId),
        refreshItems = { refreshSeriesItemsUseCase(it) },
    )

    private val groupImagesCache = mutableMapOf<String, StateFlow<List<Image>>>()

    /** One group's images, for its tile's count badge; as on the designs screen, only for tiles on screen. */
    fun imagesForGroup(groupId: String): StateFlow<List<Image>> =
        groupImagesCache.getOrPut(groupId) {
            getGroupImagesUseCase(groupId)
                .map { it?.images.orEmpty() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        }
}
