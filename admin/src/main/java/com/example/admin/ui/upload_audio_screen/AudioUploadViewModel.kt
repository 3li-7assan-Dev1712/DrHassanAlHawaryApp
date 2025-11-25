package com.example.admin.ui.upload_audio_screen

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.UploadAudioUseCase
import com.example.domain.use_cases.audios.UploadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class AudioUploadUiState {
    object Idle : AudioUploadUiState()
    data class Loading(val progress: Int) : AudioUploadUiState()
    object Success : AudioUploadUiState()
    data class Error(val message: String) : AudioUploadUiState()
}

@HiltViewModel
class AudioUploadViewModel @Inject constructor(
    private val uploadAudioUseCase: UploadAudioUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<AudioUploadUiState>(
        AudioUploadUiState.Idle
    )
    val uiState = _uiState.asStateFlow()

    private val _audioTitle = MutableStateFlow("")
    val audioTitle = _audioTitle.asStateFlow()

    private val _selectedAudioUri = MutableStateFlow<Uri?>(null)
    val selectedAudioUri = _selectedAudioUri.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _audioTitle.value = newTitle
    }

    fun onAudioSelected(uri: Uri) {
        _selectedAudioUri.value = uri
    }

    fun uploadAudio() {
        val title = _audioTitle.value
        val uri = _selectedAudioUri.value

        // Validate inputs
        if (title.isBlank()) {
            _uiState.value = AudioUploadUiState.Error("Title cannot be empty.")
            return
        }
        if (uri == null) {
            _uiState.value =
                AudioUploadUiState.Error("Please select an audio file.")
            return
        }
        viewModelScope.launch {
            uploadAudioUseCase(
                title = title,
                uriString = uri.toString(), // Pass the Uri as a primitive string
                durationInMillis = getAudioDuration(uri) ?: 0L
            ).collect { result ->
                // --- State Update ---
                when (result) {
                    is UploadResult.Progress -> {
                        _uiState.value =
                            AudioUploadUiState.Loading(result.percentage)
                    }

                    is UploadResult.Success -> {
                        _uiState.value = AudioUploadUiState.Success
                        // Optionally reset the state after a brief success message
                        // For example:
                        delay(2000)
                        resetState()
                    }

                    is UploadResult.Error -> {
                        _uiState.value = AudioUploadUiState.Error(result.message)
                    }
                }
            }

        }
    }

    private fun getAudioDuration(uri: Uri): Long? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLong()
        } catch (_: Exception) {
            null
        }
    }

    // Resets the state for a new upload after success
    fun resetState() {
        _audioTitle.value = ""
        _selectedAudioUri.value = null
        _uiState.value = AudioUploadUiState.Idle
    }


}




