package com.example.study.presentation.lessons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.use_case.GetLessonsForPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(

    val getLessonsForPlaylistUseCase: GetLessonsForPlaylistUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "LessonsViewModel"

    private val _uiState = MutableStateFlow<LessonsUiState>(LessonsUiState.Loading)

    val uiState = _uiState.asStateFlow()
    val playlistId = savedStateHandle.get<String>("playlistId")

    init {
        viewModelScope.launch {
            try {
                if (playlistId != null) {
                    getLessonsForPlaylistUseCase(playlistId).collect { lessons ->
                        if (!lessons.isNullOrEmpty()) _uiState.value =
                            LessonsUiState.Success(lessons)
                        else _uiState.value = LessonsUiState.Error("No lessons found")
                    }
                } else {
                    throw NullPointerException("levelId is null")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = LessonsUiState.Error("${e.message}")
            }
        }
    }

}
