package com.example.admin.ui.upload_images_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.ImageGroup
import com.example.domain.use_cases.images.DeleteImageGroupUseCase
import com.example.domain.use_cases.images.GetAllRemoteImageGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImageGroupsListUiState {
    object Loading : ImageGroupsListUiState
    data class Success(val groups: List<ImageGroup>) : ImageGroupsListUiState
    data class Error(val message: String) : ImageGroupsListUiState
}

@HiltViewModel
class ImageGroupsListViewModel @Inject constructor(
    private val getAllRemoteImageGroupsUseCase: GetAllRemoteImageGroupsUseCase,
    private val deleteImageGroupUseCase: DeleteImageGroupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImageGroupsListUiState>(ImageGroupsListUiState.Loading)
    val uiState: StateFlow<ImageGroupsListUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { ImageGroupsListUiState.Loading }
            try {
                val groups = getAllRemoteImageGroupsUseCase()
                _uiState.update { ImageGroupsListUiState.Success(groups) }
            } catch (e: Exception) {
                _uiState.update { ImageGroupsListUiState.Error(e.message ?: "Failed to load image groups") }
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            deleteImageGroupUseCase(groupId).onSuccess {
                loadGroups()
            }
        }
    }
}
