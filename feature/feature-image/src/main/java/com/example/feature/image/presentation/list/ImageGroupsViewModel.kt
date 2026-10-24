package com.example.feature.image.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.feature.image.domain.use_case.GetGroupImagesUseCase
import com.example.feature.image.domain.use_case.GetImageGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


import javax.inject.Inject


@HiltViewModel
class ImagesGroupsViewModel @Inject constructor(
    private val getImageGroupsUseCase: GetImageGroupsUseCase,
    private val getGroupImagesUseCase: GetGroupImagesUseCase,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
) : ViewModel() {


    val imageGroups: Flow<PagingData<ImageGroup>> = getImageGroupsUseCase()
        .cachedIn(viewModelScope)

    /** The design series ("رمضانيات"), shown above the standalone designs. */
    val series: StateFlow<List<Series>> = getSeriesUseCase(SeriesCollection.IMAGE_GROUPS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Network failures are already logged; a cache write failing leaves the cached series.
        viewModelScope.launch { runCatching { syncSeriesUseCase() } }
    }

    private val groupImagesCache = mutableMapOf<String, StateFlow<List<Image>>>()

    /**
     * One group's images as a cached, lazily-started [StateFlow] - collection (and
     * the underlying local/remote fetch inside [GetGroupImagesUseCase]) only starts
     * once a group's row is actually composed and observing it, i.e. only for rows
     * currently on or near screen, and stops a few seconds after the row scrolls
     * away ([SharingStarted.WhileSubscribed]) rather than loading every group's
     * images up front.
     */
    fun imagesForGroup(groupId: String): StateFlow<List<Image>> =
        groupImagesCache.getOrPut(groupId) {
            getGroupImagesUseCase(groupId)
                .map { it?.images.orEmpty() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        }

}
