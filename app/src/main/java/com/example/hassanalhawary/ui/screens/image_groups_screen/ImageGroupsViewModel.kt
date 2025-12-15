package com.example.hassanalhawary.ui.screens.image_groups_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.ImageGroup
import com.example.domain.use_cases.images.GetImageGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


// Represents the state for the screen that displays all image groups.
sealed class ImageGroupsUiState {
    data object Loading : ImageGroupsUiState()
    data class Success(val groups: List<ImageGroup>) : ImageGroupsUiState()
    data class Error(val message: String) : ImageGroupsUiState()
}

@HiltViewModel
class ImagesGroupsViewModel @Inject constructor(
    private val getImageGroupsUseCase: GetImageGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImageGroupsUiState>(ImageGroupsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchGroups()
    }

    private fun fetchGroups() {
        getImageGroupsUseCase().onEach { groups ->
            _uiState.value = if (groups.isNotEmpty()) {
                ImageGroupsUiState.Success(groups)
            } else {
                // You might want a specific state for emptiness, but Success with an empty list is also fine.
                ImageGroupsUiState.Success(emptyList())
            }
        }.launchIn(viewModelScope)
    }
}