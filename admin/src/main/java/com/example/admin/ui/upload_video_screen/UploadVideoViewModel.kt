package com.example.admin.ui.upload_video_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.UploadResult
import com.example.domain.use_cases.videos.GetVideoByIdUseCase
import com.example.domain.use_cases.videos.UpdateVideoUseCase
import com.example.domain.use_cases.videos.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val updateVideoUseCase: UpdateVideoUseCase,
    private val getVideoByIdUseCase: GetVideoByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var title by mutableStateOf("")
    var videoUrl by mutableStateOf("")
    var type by mutableStateOf("")
    var videoId: String? by mutableStateOf(savedStateHandle["videoId"])

    private val _uploadState = MutableStateFlow<UploadResult?>(null)
    val uploadState: StateFlow<UploadResult?> = _uploadState

    init {
        videoId?.let { id ->
            loadVideo(id)
        }
    }

    private fun loadVideo(id: String) {
        viewModelScope.launch {
            _uploadState.value = UploadResult.Progress(0)
            val video = getVideoByIdUseCase(id)
            if (video != null) {
                title = video.title
                videoUrl = video.videoUrl
                type = video.type
                _uploadState.value = null
            } else {
                _uploadState.value = UploadResult.Error("Failed to load video data")
            }
        }
    }

    fun uploadVideo() {
        if (title.isBlank() || videoUrl.isBlank()) {
            _uploadState.value = UploadResult.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            val flow = if (videoId == null) {
                uploadVideoUseCase(title, videoUrl)
            } else {
                updateVideoUseCase(videoId!!, title, videoUrl)
            }

            flow.collect { result ->
                _uploadState.value = result
                if (result is UploadResult.Success && videoId == null) {
                    title = ""
                    videoUrl = ""
                    type = ""
                }
            }
        }
    }

    fun resetState() {
        _uploadState.value = null
    }
}
