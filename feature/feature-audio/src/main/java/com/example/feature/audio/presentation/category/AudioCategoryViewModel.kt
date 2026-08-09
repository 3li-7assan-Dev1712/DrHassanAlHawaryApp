package com.example.feature.audio.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.GetAudioCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCategoryViewModel @Inject constructor(
    private val getAudioCategoriesUseCase: GetAudioCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioCategoryUiState())
    val uiState = _uiState.asStateFlow()

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
