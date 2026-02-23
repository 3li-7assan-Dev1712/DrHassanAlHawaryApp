package com.example.admin.ui.upload_motivational_messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed interface UiState {
    data class Stable(
        val messagesText: String = "",
        val isUploading: Boolean = false,
        val error: String? = null
    ) : UiState

    object UploadSuccess : UiState
}

@HiltViewModel
class UploadMotivationalMessagesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Stable())
    val uiState = _uiState.asStateFlow()

    fun onMessagesTextChange(text: String) {
        if (_uiState.value is UiState.Stable) {
            _uiState.update { (it as UiState.Stable).copy(messagesText = text) }
        }
    }

    fun onUploadClick() {
        val currentState = _uiState.value as? UiState.Stable ?: return

        if (currentState.messagesText.isBlank()) {
            // Use the already-cast 'currentState' variable
            _uiState.update { currentState.copy(error = "Messages cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState.copy(isUploading = true, error = null) }
            try {
                // Split the text into a list of non-empty messages
                val messagesList = currentState.messagesText.lines().filter { it.isNotBlank() }
                // Create a data map to be stored in a single document
                val data = hashMapOf("messages" to messagesList)

                // Set the list in a specific document, overwriting any existing one
                firestore.collection("motivational_messages").document("daily_messages").set(data).await()

                _uiState.value = UiState.UploadSuccess

            } catch (e: Exception) {
                _uiState.update { currentState.copy(isUploading = false, error = e.message ?: "Upload failed.") }
            }
        }
    }
}