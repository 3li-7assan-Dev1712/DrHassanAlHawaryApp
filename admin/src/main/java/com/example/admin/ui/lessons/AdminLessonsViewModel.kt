package com.example.admin.ui.lessons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.DeleteLessonUseCase
import com.example.domain.use_cases.study.GetRemoteLessonsForPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminLessonsViewModel @Inject constructor(
    private val getRemoteLessonsForPlaylistUseCase: GetRemoteLessonsForPlaylistUseCase,
    private val deleteLessonUseCase: DeleteLessonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "AdminLessonsViewModel"

    val playlistId: String? = savedStateHandle["playlistId"]

    private val _uiState = MutableStateFlow<AdminLessonsUiState>(AdminLessonsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadLessons()
    }

    fun loadLessons() {
        viewModelScope.launch {
            _uiState.value = AdminLessonsUiState.Loading
            try {
                if (playlistId != null) {
                    val lessons = getRemoteLessonsForPlaylistUseCase(playlistId)
                    if (lessons.isNotEmpty()) _uiState.value =
                        AdminLessonsUiState.Success(lessons)
                    else _uiState.value = AdminLessonsUiState.Error("No lessons found")
                } else {
                    _uiState.value = AdminLessonsUiState.Error("playlistId is null")
                }
            } catch (e: Exception) {
                _uiState.value = AdminLessonsUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun deleteLesson(lessonId: String) {
        viewModelScope.launch {
            deleteLessonUseCase(lessonId).onSuccess {
                loadLessons()
            }.onFailure {
                // Handle error
            }
        }
    }
}
