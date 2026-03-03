package com.example.study.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.DisconnectTelegramUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.StoreStudentDataUseCase
import com.example.study.domain.use_case.GetStudentAuthDataUseCase
import com.example.study.presentation.model.StudyScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    getStudentDataUseCase: GetStudentDataUseCase,
    private val storeStudentDataUseCase: StoreStudentDataUseCase,
    private val disconnectTelegramUseCase: DisconnectTelegramUseCase,
    private val getStudentAuthDataUseCase: GetStudentAuthDataUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "StudyViewModel"

    val uiState: StateFlow<StudyScreenUiState> = getStudentDataUseCase()
        .map { studentData ->
            if (studentData != null) StudyScreenUiState.StudentDashboard(studentData)
            else StudyScreenUiState.Guest
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StudyScreenUiState.Loading
        )

    private val dataFlow = savedStateHandle.getStateFlow<String?>("data", null)

    init {
        viewModelScope.launch {
            dataFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect { encodedData ->
                    Log.d(TAG, "deep link data received: $encodedData")

                    val json = Uri.decode(encodedData)
                    val user = JSONObject(json)
                    val telegramId = user.getLong("id")
                    Log.d(TAG, "telegramId: $telegramId")

                    val uid = getStudentAuthDataUseCase()?.userId
                    if (uid != null) {
                        storeStudentDataUseCase(uid)
                        Log.d(TAG, "uid: $uid")

                    } else {
                        Log.d(TAG, "uid is null: ")
                    }
                }
        }
    }

    fun onDisconnectTelegram() {
        viewModelScope.launch {
            disconnectTelegramUseCase()
        }
    }
}