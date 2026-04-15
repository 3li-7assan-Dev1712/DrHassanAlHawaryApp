package com.example.study.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.CheckStudentChannelMembershipStateUseCase
import com.example.domain.use_cases.study.DisconnectTelegramUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.StoreStudentDataUseCase
import com.example.study.domain.use_case.GetStudentAuthDataUseCase
import com.example.study.presentation.model.StudyScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
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
    private val checkStudentMembership: CheckStudentChannelMembershipStateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "StudyViewModel"

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<StudyScreenUiState> = combine(
        getStudentDataUseCase(),
        _isLoading
    ) { studentData, isLoading ->
        if (isLoading) {
            StudyScreenUiState.Loading
        } else {
            if (studentData != null) {
                if (studentData.membershipState == "none") {
                    StudyScreenUiState.NotChannelMember(studentData)
                } else {
                    StudyScreenUiState.StudentDashboard(studentData)
                }
            } else StudyScreenUiState.Guest
        }
    }
        .catch { e ->
            Log.e(TAG, "Firestore Permission Error in uiState", e)
            emit(StudyScreenUiState.Guest)
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
                    _isLoading.value = true
                    try {
                        val json = Uri.decode(encodedData)
                        val user = JSONObject(json)
                        val telegramId = user.getLong("telegramId")
                        Log.d(TAG, "telegramId: $telegramId")

                        val uid = getStudentAuthDataUseCase()?.userId
                        if (uid != null) {
                            Log.d(TAG, "Checking membership and storing data for uid: $uid")
                            checkStudentMembership(uid, telegramId)
                            storeStudentDataUseCase(uid)
                        } else {
                            Log.d(TAG, "uid is null: ")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing deep link data", e)
                    } finally {
                        _isLoading.value = false

                        // here when user navigate to home screen
                    }
                }

        }
    }

    fun onRefreshStudentData() {
        val studentData = (uiState.value as? StudyScreenUiState.NotChannelMember)?.studentData
        val telegramId = studentData?.telegramId
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = getStudentAuthDataUseCase()?.userId
                if (uid != null && telegramId != null) {
                    checkStudentMembership(uid, telegramId)
                    storeStudentDataUseCase(uid)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing student data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    
    fun onDisconnectTelegram() {
        viewModelScope.launch {
            disconnectTelegramUseCase()
        }
    }
}
