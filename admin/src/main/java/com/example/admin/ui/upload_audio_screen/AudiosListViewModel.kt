package com.example.admin.ui.upload_audio_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Audio
import com.example.domain.use_cases.audios.GetAllRemoteAudiosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AudiosListUiState {
    object Loading : AudiosListUiState
    data class Success(val audios: List<Audio>) : AudiosListUiState
    data class Error(val message: String) : AudiosListUiState
}

@HiltViewModel
class AudiosListViewModel @Inject constructor(
    private val getAllRemoteAudiosUseCase: GetAllRemoteAudiosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AudiosListUiState>(AudiosListUiState.Loading)
    val uiState: StateFlow<AudiosListUiState> = _uiState.asStateFlow()

    init {
        loadAudios()
    }

    fun loadAudios() {
        viewModelScope.launch {
            _uiState.update { AudiosListUiState.Loading }
            try {
                val audios = getAllRemoteAudiosUseCase()
                _uiState.update { AudiosListUiState.Success(audios) }
            } catch (e: Exception) {
                _uiState.update { AudiosListUiState.Error(e.message ?: "Failed to load audios") }
            }
        }
    }
}
