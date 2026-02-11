package com.example.admin.ui.add_edit_playlist

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Playlist
import com.example.domain.use_cases.audios.UploadResult
import com.example.domain.use_cases.study.GetPlaylistByIdUseCase
import com.example.domain.use_cases.study.UpdatePlaylistUseCase
import com.example.domain.use_cases.study.UploadPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/*

class GetPlaylistUseCase @Inject constructor() {
    suspend fun invoke(playlistId: String): Result<Playlist> {
        // Simulate network delay
        delay(1000)
        // Return a fake playlist

        return Result.success(
            Playlist(
                id = playlistId,
                title = "Fetched Playlist Title",
                imageUrl = "https://piscum.photos/200",
                order = 1
            )
        )
    }
}
// --- End of Domain Layer ---
*/

@HiltViewModel
class AddEditPlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlaylistUseCase: GetPlaylistByIdUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val uploadPlaylistUseCase: UploadPlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddEditPlaylistUiState>(AddEditPlaylistUiState.Loading)
    val uiState = _uiState.asStateFlow()
    val playlistIdToAddEdit: String? = savedStateHandle["playlistId"]
    val levelId: String = savedStateHandle["levelId"] ?: ""

    val TAG = "AddEditPlaylistViewModel"

    init {

        viewModelScope.launch {
            if (playlistIdToAddEdit != null) {
                // edit

                Log.d(TAG, "levelId: $levelId playlistId: $playlistIdToAddEdit")
                _uiState.value = AddEditPlaylistUiState.Loading
                val playlist = getPlaylistUseCase(playlistIdToAddEdit)
                if (playlist != null) {
                    _uiState.value = AddEditPlaylistUiState.Stable(
                        playlistId = playlist.id,
                        levelId = playlist.levelId,
                        title = playlist.title,
                        order = playlist.order.toString(),
                        existingImageUrl = playlist.thumbnailUrl
                    )

                } else {
                    // Handle the case where the playlist is not found
                    _uiState.value = AddEditPlaylistUiState.Error("Playlist not found")
                }
            } else {
                // add
                _uiState.value = AddEditPlaylistUiState.Stable(playlistId = null, levelId = levelId)


            }
        }
    }

    fun onTitleChange(newTitle: String) {
        if (_uiState.value is AddEditPlaylistUiState.Stable) {
            _uiState.update {
                (it as AddEditPlaylistUiState.Stable).copy(title = newTitle)
            }
        }
    }

    fun onOrderChange(newOrder: String) {
        if (_uiState.value is AddEditPlaylistUiState.Stable && newOrder.all { it.isDigit() }) {
            _uiState.update {
                (it as AddEditPlaylistUiState.Stable).copy(order = newOrder)
            }
        }
    }

    fun onLevelChange(newLevel: String) {
        if (_uiState.value is AddEditPlaylistUiState.Stable && newLevel.all { it.isDigit() }) {
            _uiState.update {
                (it as AddEditPlaylistUiState.Stable).copy(levelId = newLevel)
            }
        }
    }

    fun onImageSelected(uri: Uri?) {
        if (_uiState.value is AddEditPlaylistUiState.Stable) {
            _uiState.update {
                (it as AddEditPlaylistUiState.Stable).copy(selectedImageUri = uri)
            }
        }
    }

    fun onSaveClick() {
        if (_uiState.value is AddEditPlaylistUiState.Stable) {
            viewModelScope.launch {
                // Set loading state for saving
                _uiState.update { (it as AddEditPlaylistUiState.Stable).copy(isSaving = true) }

                if (playlistIdToAddEdit != null) {
                    // edit
                    val playlistToEdit = Playlist(
                        id = playlistIdToAddEdit,
                        title = (uiState.value as AddEditPlaylistUiState.Stable).title,
                        order = (uiState.value as AddEditPlaylistUiState.Stable).order.toInt(),
                        levelId = levelId,
                        thumbnailUrl = (uiState.value as AddEditPlaylistUiState.Stable).selectedImageUri?.toString()
                            ?: ""
                    )
                    updatePlaylistUseCase(playlistToEdit).onSuccess {

                        _uiState.value = AddEditPlaylistUiState.SaveSuccess
                    }.onFailure {
                        _uiState.value = AddEditPlaylistUiState.Error(it.message ?: "Unknown error")
                    }
                } else {
                    val newPlaylist = Playlist(
                        id = "",
                        levelId = levelId,
                        title = (uiState.value as AddEditPlaylistUiState.Stable).title,
                        order = (uiState.value as AddEditPlaylistUiState.Stable).order.toInt(),
                        thumbnailUrl = (uiState.value as AddEditPlaylistUiState.Stable).selectedImageUri.toString()
                    )
                    uploadPlaylistUseCase(
                        newPlaylist
                    ).collect {
                        when (it) {
                            is UploadResult.Error -> {
                                _uiState.value = AddEditPlaylistUiState.Error(it.message)
                                return@collect
                            }

                            is UploadResult.Progress -> {
                                _uiState.value = AddEditPlaylistUiState.Stable(
                                    playlistId = null,
                                    levelId = levelId,
                                    isSaving = true
                                )
                            }

                            is UploadResult.Success -> {
                                _uiState.value = AddEditPlaylistUiState.SaveSuccess
                            }
                        }
                    }
                }

                // Simulate saving to server
                delay(1500)
                // Handle success
                _uiState.value = AddEditPlaylistUiState.SaveSuccess
            }
        }
    }
}
