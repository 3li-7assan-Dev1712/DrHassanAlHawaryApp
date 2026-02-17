package com.example.admin.ui.add_edit_lesson

import android.net.Uri

sealed interface AddEditLessonUiState {
    data class Stable(
        val lessonId: String?,
        val title: String = "",
        val order: String = "",
        val existingPdfUrl: String? = null,
        val existingAudioUrl: String? = null,
        val selectedPdfUri: Uri? = null,
        val selectedAudioUri: Uri? = null,
        val isSaving: Boolean = false,
        val error: String? = null
    ) : AddEditLessonUiState

    object Loading : AddEditLessonUiState
    object SaveSuccess : AddEditLessonUiState
    data class Error(val message: String) : AddEditLessonUiState
}