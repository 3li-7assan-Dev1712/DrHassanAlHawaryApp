package com.example.study.presentation.playlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.use_case.GetPlaylistsForLevelUseCase
import com.example.study.domain.use_case.SyncLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    getPlaylistsForLevelUseCase: GetPlaylistsForLevelUseCase,
    syncLessonsUseCase: SyncLessonsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "PlaylistViewModel"

    val levelId = savedStateHandle.get<String>("levelId")

    val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                if (levelId != null) {
                    getPlaylistsForLevelUseCase(levelId).collect { playlists ->

                        Log.d(TAG, "levelId is: $levelId")
                        if (!playlists.isNullOrEmpty()) {
                            _uiState.value =
                                PlaylistUiState.Success(playlists)
                            syncLessonsUseCase()
                        } else _uiState.value = PlaylistUiState.Error("No playlists found")
                    }
                } else {
                    throw NullPointerException("levelId is null")
                }
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
            }
        }
    }

}
