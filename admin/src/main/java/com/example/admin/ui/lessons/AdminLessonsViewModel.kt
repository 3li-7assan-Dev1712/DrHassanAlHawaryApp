package com.example.admin.ui.lessons

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.GetRemoteLessonsForPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminLessonsViewModel @Inject constructor(
    getRemoteLessonsForPlaylistUseCase: GetRemoteLessonsForPlaylistUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "AdminLessonsViewModel"

//    val levelId = "savedStateHandle.get<String>("levelId")"

    val playlistId: String? = savedStateHandle["playlistId"]

    private val _uiState = MutableStateFlow<AdminLessonsUiState>(AdminLessonsUiState.Loading)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                if (playlistId != null) {
                    val lessons = getRemoteLessonsForPlaylistUseCase(playlistId)
                    Log.d(TAG, "levelId is: $playlistId")
                    if (lessons.isNotEmpty()) _uiState.value =
                        AdminLessonsUiState.Success(lessons)
                    else _uiState.value = AdminLessonsUiState.Error("No playlists found")

                } else {
                    throw NullPointerException("levelId is null")
                }
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
            }
        }
    }

}
