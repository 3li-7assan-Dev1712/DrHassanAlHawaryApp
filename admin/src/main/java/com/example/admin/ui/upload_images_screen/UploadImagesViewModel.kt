package com.example.admin.ui.upload_images_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.UploadResult
import com.example.domain.use_cases.images.UploadImageGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Sealed class to represent the different states of the UI for the design upload screen.
 * This makes state management predictable and type-safe.
 */
sealed class UploadImagesUiState {
    data object Idle : UploadImagesUiState()
    data class Loading(val progress: Int) : UploadImagesUiState()
    data object Success : UploadImagesUiState()
    data class Error(val message: String) : UploadImagesUiState()
}

@HiltViewModel
class UploadImagesViewModel @Inject constructor(
    private val uploadImageGroupUseCase: UploadImageGroupUseCase
) : ViewModel() {

    // --- State Holders ---

    private val _uiState = MutableStateFlow<UploadImagesUiState>(UploadImagesUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _groupTitle = MutableStateFlow("")
    val groupTitle = _groupTitle.asStateFlow()

    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris = _selectedImageUris.asStateFlow()

    // --- User Actions ---

    fun onTitleChange(newTitle: String) {
        _groupTitle.value = newTitle
    }

    fun onImagesSelected(uris: List<Uri>) {
        _selectedImageUris.value = uris
    }

    suspend fun uploadDesignGroup() {
        val title = _groupTitle.value
        val uris = _selectedImageUris.value

        if (title.isBlank()) {
            _uiState.value = UploadImagesUiState.Error("Title cannot be empty.")
            return
        }
        if (uris.isEmpty()) {
            _uiState.value = UploadImagesUiState.Error("Please select at least one image.")
            return
        }

        // --- Logic Execution ---

        uploadImageGroupUseCase(title, uris.map { it.toString() })
            .onEach { result ->
                when (result) {
                    is UploadResult.Progress -> {
                        _uiState.value = UploadImagesUiState.Loading(result.percentage)
                    }
                    is UploadResult.Success -> {
                        _uiState.value = UploadImagesUiState.Success
                        // Automatically reset after a short delay for a better user experience
                        delay(2000)
                        resetState()
                    }
                    is UploadResult.Error -> {
                        _uiState.value = UploadImagesUiState.Error(result.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Resets all state holders to their initial values, ready for a new upload.
     */
    fun resetState() {
        _groupTitle.value = ""
        _selectedImageUris.value = emptyList()
        _uiState.value = UploadImagesUiState.Idle
    }
}