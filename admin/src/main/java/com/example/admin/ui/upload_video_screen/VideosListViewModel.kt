package com.example.admin.ui.upload_video_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Video
import com.example.domain.use_cases.videos.DeleteVideoUseCase
import com.example.domain.use_cases.videos.GetAllRemoteVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VideosListUiState {
    object Loading : VideosListUiState
    data class Success(val videos: List<Video>) : VideosListUiState
    data class Error(val message: String) : VideosListUiState
}

@HiltViewModel
class VideosListViewModel @Inject constructor(
    private val getAllRemoteVideosUseCase: GetAllRemoteVideosUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideosListUiState>(VideosListUiState.Loading)
    val uiState: StateFlow<VideosListUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { VideosListUiState.Loading }
            try {
                val videos = getAllRemoteVideosUseCase()
                _uiState.update { VideosListUiState.Success(videos) }
            } catch (e: Exception) {
                _uiState.update { VideosListUiState.Error(e.message ?: "Failed to load videos") }
            }
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            deleteVideoUseCase(videoId).onSuccess {
                loadVideos()
            }
        }
    }
}
