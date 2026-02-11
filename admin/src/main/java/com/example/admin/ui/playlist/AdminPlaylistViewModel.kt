package com.example.admin.ui.playlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.GetRemotePlaylistsForLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPlaylistViewModel @Inject constructor(
    getPlaylistsForLevelUseCase: GetRemotePlaylistsForLevelUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "PlaylistViewModel"

//    val levelId = "savedStateHandle.get<String>("levelId")"

    val levelId: String? = savedStateHandle["levelId"]

    val _uiState = MutableStateFlow<AdminPlaylistUiState>(AdminPlaylistUiState.Loading)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                if (levelId != null) {
                    val playlists = getPlaylistsForLevelUseCase(levelId)
                    Log.d(TAG, "levelId is: $levelId")
                    if (playlists.isNotEmpty()) _uiState.value =
                        AdminPlaylistUiState.Success(playlists)
                    else _uiState.value = AdminPlaylistUiState.Error("No playlists found")

                } else {
                    throw NullPointerException("levelId is null")
                }
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
            }
        }
    }

}
