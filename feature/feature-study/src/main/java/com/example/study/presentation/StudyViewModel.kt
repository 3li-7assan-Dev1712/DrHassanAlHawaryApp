package com.example.study.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.GetUserDataUseCase
import com.example.domain.use_cases.study.SyncUserUseCase
import com.example.study.presentation.model.StudyScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val syncUserUseCase: SyncUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "StudyViewModel"

    // Driven purely by Room (Local Source of Truth)
    val uiState: StateFlow<StudyScreenUiState> = getUserDataUseCase()
        .map { user ->
            when {
                user == null -> StudyScreenUiState.Guest
                !user.isConnectedToTelegram -> StudyScreenUiState.Guest
                user.membershipState == "none" -> StudyScreenUiState.NotAllowed
                else -> StudyScreenUiState.StudentDashboard(user)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StudyScreenUiState.Loading
        )

    init {
        // Automatically sync from remote whenever we have a local user record
        // This handles app launch, local data changes, and catch-up for Cloud Function merges.
        viewModelScope.launch {
            getUserDataUseCase().collectLatest { user ->
                if (user != null && !user.isConnectedToTelegram) {
                    try {
                        syncUserUseCase(user.uid)
                    } catch (e: Exception) {
                        Log.e(TAG, "Sync failed: ${e.message}")
                    }
                }
            }
        }
    }
}
