package com.example.admin.ui.upload_video_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.UploadResult
import com.example.domain.use_cases.videos.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val uploadVideoUseCase: UploadVideoUseCase
) : ViewModel() {

    var title by mutableStateOf("")
    var videoUrl by mutableStateOf("")

    private val _uploadState = MutableStateFlow<UploadResult?>(null)
    val uploadState: StateFlow<UploadResult?> = _uploadState

    fun uploadVideo() {
        if (title.isBlank() || videoUrl.isBlank()) {
            _uploadState.value = UploadResult.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            uploadVideoUseCase(title, videoUrl).collect { result ->
                _uploadState.value = result
                if (result is UploadResult.Success) {
                    title = ""
                    videoUrl = ""
                }
            }
        }
    }

    fun resetState() {
        _uploadState.value = null
    }
}