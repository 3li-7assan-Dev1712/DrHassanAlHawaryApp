package com.example.admin.ui.upload_audio_screen

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.GetAudioByIdUseCase
import com.example.domain.use_cases.audios.UpdateAudioUseCase
import com.example.domain.use_cases.audios.UploadAudioUseCase
import com.example.domain.use_cases.audios.UploadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioUploadUiState(
    val audioId: String? = null,
    val title: String = "",
    val selectedUri: Uri? = null,
    val existingUrl: String? = null,
    val isUploading: Boolean = false,
    val progress: Int = 0,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AudioUploadViewModel @Inject constructor(
    private val uploadAudioUseCase: UploadAudioUseCase,
    private val updateAudioUseCase: UpdateAudioUseCase,
    private val getAudioByIdUseCase: GetAudioByIdUseCase,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUploadUiState())
    val uiState = _uiState.asStateFlow()

    private val audioId: String? = savedStateHandle["audioId"]

    init {
        if (audioId != null) {
            loadAudio(audioId)
        }
    }

    private fun loadAudio(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            val audio = getAudioByIdUseCase(id)
            if (audio != null) {
                _uiState.update {
                    it.copy(
                        audioId = audio.id,
                        title = audio.title,
                        existingUrl = audio.audioUrl,
                        isUploading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isUploading = false, error = "Failed to load audio data") }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onAudioSelected(uri: Uri) {
        _uiState.update { it.copy(selectedUri = uri) }
    }

    fun saveAudio() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        if (currentState.audioId == null && currentState.selectedUri == null) {
            _uiState.update { it.copy(error = "Please select an audio file") }
            return
        }

        viewModelScope.launch {
            val duration = currentState.selectedUri?.let { getAudioDuration(it) } ?: 0L
            
            val flow = if (currentState.audioId == null) {
                uploadAudioUseCase(
                    title = currentState.title,
                    uriString = currentState.selectedUri.toString(),
                    durationInMillis = duration
                )
            } else {
                updateAudioUseCase(
                    id = currentState.audioId,
                    title = currentState.title,
                    newUriString = currentState.selectedUri?.toString(),
                    existingUrl = currentState.existingUrl ?: "",
                    durationInMillis = duration
                )
            }

            flow.collect { result ->
                when (result) {
                    is UploadResult.Progress -> {
                        _uiState.update { it.copy(isUploading = true, progress = result.percentage) }
                    }
                    is UploadResult.Success -> {
                        _uiState.update { it.copy(isUploading = false, isSuccess = true) }
                    }
                    is UploadResult.Error -> {
                        _uiState.update { it.copy(isUploading = false, error = result.message) }
                    }
                }
            }
        }
    }

    private fun getAudioDuration(uri: Uri): Long? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLong()
        } catch (_: Exception) {
            null
        }
    }

    fun resetState() {
        _uiState.value = AudioUploadUiState()
    }
}
