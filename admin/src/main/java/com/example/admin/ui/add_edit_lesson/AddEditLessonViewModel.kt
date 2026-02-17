package com.example.admin.ui.add_edit_lesson

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Lesson
import com.example.domain.use_cases.audios.UploadResult
import com.example.domain.use_cases.study.AddLessonUseCase
import com.example.domain.use_cases.study.GetRemoteLessonByIdUseCase
import com.example.domain.use_cases.study.UpdateLessonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddEditLessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLessonByIdUseCase: GetRemoteLessonByIdUseCase,
    private val addLessonUseCase: AddLessonUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase
) : ViewModel() {

    private val TAG = "AddEditLessonViewModel"

    private val _uiState = MutableStateFlow<AddEditLessonUiState>(AddEditLessonUiState.Loading)
    val uiState = _uiState.asStateFlow()


    val lessonId: String? = savedStateHandle["lessonId"]
    val playlistId: String? = savedStateHandle["playlistId"]

    init {

        viewModelScope.launch {
            if (lessonId != null) {
                // edit
                _uiState.value = AddEditLessonUiState.Loading
                val lesson = getLessonByIdUseCase(lessonId)
                Log.d(TAG, "init: LessonId: $lessonId palylistId: $playlistId")
                if (lesson != null) {
                    _uiState.value = AddEditLessonUiState.Stable(
                        lessonId = lesson.id,
                        title = lesson.title,
                        order = lesson.order.toString(),
                        existingPdfUrl = lesson.pdfUrl,
                        existingAudioUrl = lesson.audioUrl
                    )
                } else {
                    _uiState.value = AddEditLessonUiState.Error("Failed to load lesson.")
                }

            } else {
                // add
                _uiState.value = AddEditLessonUiState.Stable(lessonId = null)
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        if (_uiState.value is AddEditLessonUiState.Stable) {
            _uiState.update { (it as AddEditLessonUiState.Stable).copy(title = newTitle) }
        }
    }

    fun onOrderChange(newOrder: String) {
        if (_uiState.value is AddEditLessonUiState.Stable && newOrder.all { it.isDigit() }) {
            _uiState.update { (it as AddEditLessonUiState.Stable).copy(order = newOrder) }
        }
    }

    fun onPdfSelected(uri: Uri?) {
        if (_uiState.value is AddEditLessonUiState.Stable) {
            _uiState.update { (it as AddEditLessonUiState.Stable).copy(selectedPdfUri = uri) }
        }
    }

    fun onAudioSelected(uri: Uri?) {
        if (_uiState.value is AddEditLessonUiState.Stable) {
            _uiState.update { (it as AddEditLessonUiState.Stable).copy(selectedAudioUri = uri) }
        }
    }

    fun onClearPdf() = onPdfSelected(null)
    fun onClearAudio() = onAudioSelected(null)

    fun onSaveClick() {
        val currentState = _uiState.value
        if (currentState is AddEditLessonUiState.Stable) {
            // Basic validation
            if (currentState.title.isBlank() || currentState.order.isBlank()) {
                _uiState.update { currentState.copy(error = "Title and Order cannot be empty.") }
                return
            }

            viewModelScope.launch {
                _uiState.update { currentState.copy(isSaving = true, error = null) }
                if (lessonId != null) {
                    // Edit

                    _uiState.value = AddEditLessonUiState.Loading
                    val updatedLesson = Lesson(
                        id = lessonId,
                        title = currentState.title,
                        order = currentState.order.toInt(),
                        audioUrl = currentState.existingAudioUrl ?: "",
                        pdfUrl = currentState.existingPdfUrl ?: "",
                        duration = ""
                    )
                    updateLessonUseCase(
                        updatedLesson,
                        currentState.selectedAudioUri?.toString(),
                        currentState.selectedPdfUri?.toString()
                    ).onSuccess {
                        _uiState.value = AddEditLessonUiState.SaveSuccess

                    }
                        .onFailure {
                            _uiState.value = AddEditLessonUiState.Error("Failed to update lesson.")
                        }


                } else {
                    // add

                    Log.d(TAG, "onSaveClick: ${currentState.selectedAudioUri.toString()}")
                    Log.d(TAG, "onSaveClick: ${currentState.selectedPdfUri}")
                    val newLesson = Lesson(
                        id = "",
                        title = currentState.title,
                        order = currentState.order.toInt(),
                        audioUrl = currentState.selectedAudioUri?.toString() ?: "",
                        pdfUrl = currentState.selectedPdfUri?.toString() ?: "",
                        duration = ""
                    )
                    if (playlistId.isNullOrEmpty()) {
                        Log.d(TAG, "onSaveClick: empty playlist")
                        return@launch
                    }
                    Log.d(TAG, "onSaveClick: $playlistId")
                    addLessonUseCase(
                        newLesson,
                        playlistId
                    ).collect { result ->

                        when (result) {
                            is UploadResult.Success -> {
                                _uiState.value = AddEditLessonUiState.SaveSuccess
                            }

                            is UploadResult.Error -> {

                                _uiState.value = AddEditLessonUiState.Error(result.message)


                            }

                            is UploadResult.Progress -> {

                                _uiState.value = AddEditLessonUiState.Loading

                            }


                        }
                    }
                }
            }
        }
    }
}