package com.example.admin.ui.institute_main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.DisconnectTelegramUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.StoreAdminDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class InstituteViewModel @Inject constructor(
    getStudentDataUseCase: GetStudentDataUseCase,
    storeAdminDataUseCase: StoreAdminDataUseCase,
    private val disconnectTelegramUseCase: DisconnectTelegramUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "InstituteViewModel"


    val uiState: StateFlow<InstituteScreenUiState> = getStudentDataUseCase()
        .map { studentData ->
            if (studentData != null) {
                InstituteScreenUiState.AdminDashboard(studentData)
            } else {
                InstituteScreenUiState.Guest
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // The screen will show "Loading" on startup and while processing the deep link
            initialValue = InstituteScreenUiState.Loading,
        )


    init {
        savedStateHandle.get<String>("data")?.let { encodedData ->
            Log.d(TAG, "data: $encodedData")
            if (uiState.value !is InstituteScreenUiState.AdminDashboard) {
                val json = Uri.decode(encodedData)
                val user = JSONObject(json)
                val telegramId = user.getLong("id")
                Log.d("StudyViewModel", "telegram id: $telegramId")
                viewModelScope.launch {
                    storeAdminDataUseCase(telegramId)
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