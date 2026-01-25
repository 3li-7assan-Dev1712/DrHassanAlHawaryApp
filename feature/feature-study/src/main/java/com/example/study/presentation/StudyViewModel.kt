package com.example.study.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.use_case.DisconnectTelegramUseCase
import com.example.study.domain.use_case.GetStudentDataUseCase
import com.example.study.domain.use_case.StoreStudentDataUseCase
import com.example.study.presentation.model.StudyScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    getStudentDataUseCase: GetStudentDataUseCase,
    storeStudentDataUseCase: StoreStudentDataUseCase,
    private val disconnectTelegramUseCase: DisconnectTelegramUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    val uiState: StateFlow<StudyScreenUiState> = getStudentDataUseCase()
        .map { studentData ->
            if (studentData != null) {
                StudyScreenUiState.StudentDashboard(studentData)
            } else {
                StudyScreenUiState.Guest
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // The screen will show "Loading" on startup and while processing the deep link
            initialValue = StudyScreenUiState.Loading,
        )



    init {
        savedStateHandle.get<String>("data")?.let { encodedData ->
            if (uiState.value !is StudyScreenUiState.StudentDashboard) {
                val json = Uri.decode(encodedData)
                val user = JSONObject(json)
                val telegramId = user.getLong("id")
                Log.d("StudyViewModel", "telegram id: $telegramId")
                viewModelScope.launch {
                    storeStudentDataUseCase(telegramId)
                }
            }
        }
    }


    fun onDisconnectTelegram() {
        viewModelScope.launch {
            disconnectTelegramUseCase()
            // After disconnecting, reload the screen to show the Guest view again
        }
    }
}