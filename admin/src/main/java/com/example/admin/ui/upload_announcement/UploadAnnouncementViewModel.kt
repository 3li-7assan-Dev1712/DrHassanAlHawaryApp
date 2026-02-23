package com.example.admin.ui.upload_announcement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// --- UI State ---
sealed interface UploadAnnouncementUiState {
    data class Stable(
        val title: String = "",
        val body: String = "",
        val topic: String = "student_broadcasts", // Default topic
        val isSending: Boolean = false,
        val error: String? = null
    ) : UploadAnnouncementUiState

    object SendSuccess : UploadAnnouncementUiState
}

// --- ViewModel ---
@HiltViewModel
class UploadAnnouncementViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadAnnouncementUiState>(UploadAnnouncementUiState.Stable())
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        if (_uiState.value is UploadAnnouncementUiState.Stable) {
            _uiState.update { (it as UploadAnnouncementUiState.Stable).copy(title = newTitle) }
        }
    }

    fun onBodyChange(newBody: String) {
        if (_uiState.value is UploadAnnouncementUiState.Stable) {
            _uiState.update { (it as UploadAnnouncementUiState.Stable).copy(body = newBody) }
        }
    }

    fun onTopicChange(newTopic: String) {
        if (_uiState.value is UploadAnnouncementUiState.Stable) {
            _uiState.update { (it as UploadAnnouncementUiState.Stable).copy(topic = newTopic) }
        }
    }

    fun onSendClick() {
        val currentState = _uiState.value
        if (currentState is UploadAnnouncementUiState.Stable) {
            // Basic validation
            if (currentState.title.isBlank() || currentState.body.isBlank() || currentState.topic.isBlank()) {
                _uiState.update { currentState.copy(error = "All fields must be filled.") }
                return
            }

            viewModelScope.launch {
                _uiState.update { currentState.copy(isSending = true, error = null) }
                try {
                    val announcement = hashMapOf(
                        "title" to currentState.title,
                        "body" to currentState.body,
                        "topic" to currentState.topic,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("student_broadcasts").add(announcement).await()
                    _uiState.value = UploadAnnouncementUiState.SendSuccess

                } catch (e: Exception) {
                    _uiState.update { currentState.copy(isSending = false, error = e.message ?: "Failed to send announcement.") }
                }
            }
        }
    }
}
