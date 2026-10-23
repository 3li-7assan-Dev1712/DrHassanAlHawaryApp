package com.example.feature.audio.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.components.ContentCategories
import com.example.domain.module.SeriesCollection
import com.example.domain.text.SeriesText
import com.example.domain.use_cases.audios.GetAudioCategoriesUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCategoryViewModel @Inject constructor(
    private val getAudioCategoriesUseCase: GetAudioCategoriesUseCase,
    getSeriesUseCase: GetSeriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioCategoryUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * "١٢ سلسلة · ١٥٨ درسًا" by category id, for the categories with series ("الكل": all of
     * them, as its screen shows them all), from the cached series and their backend counts.
     * Categories have no audio counts of their own, so the others show none.
     */
    val seriesCounts: StateFlow<Map<String, String>> = getSeriesUseCase(SeriesCollection.AUDIOS)
        .map { series ->
            val byCategory = series.filter { it.categoryId != null }.groupBy { it.categoryId!! }
            (byCategory + (ContentCategories.ALL_ID to series))
                .mapNotNull { (id, inCategory) -> SeriesText.summary(inCategory)?.let { id to it } }
                .toMap()
        }
        .catch { emit(emptyMap()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getAudioCategoriesUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { categories ->
                    _uiState.update { it.copy(isLoading = false, categories = categories) }
                }
        }
    }
}
