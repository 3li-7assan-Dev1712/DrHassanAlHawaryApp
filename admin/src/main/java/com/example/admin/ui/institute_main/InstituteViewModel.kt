package com.example.admin.ui.institute_main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.GetUserIdTokenUseCase
import com.example.domain.use_cases.study.CheckStudentChannelMembershipStateUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.StoreStudentDataUseCase
import com.example.study.domain.use_case.GetStudentAuthDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class InstituteViewModel @Inject constructor(
    val getStudentDataUseCase: GetStudentDataUseCase,
    val storeStudentDataUseCase: StoreStudentDataUseCase,
    val getUserIdTokenUseCase: GetUserIdTokenUseCase,
    private val getStudentAuthDataUseCase: GetStudentAuthDataUseCase,
    private val checkStudentMembership: CheckStudentChannelMembershipStateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "InstituteViewModel"

    private val _isRefreshing = MutableStateFlow(false)
    private val _isInitialCheckDone = MutableStateFlow(false)

    val uiState: StateFlow<InstituteScreenUiState> = combine(
        getStudentDataUseCase(),
        _isRefreshing,
        _isInitialCheckDone
    ) { studentData, isRefreshing, isInitialCheckDone ->
        if (!isInitialCheckDone) {
            InstituteScreenUiState.Loading
        } else if (studentData != null && (studentData.membershipState == "administrator" || studentData.membershipState == "creator")) {
            InstituteScreenUiState.AdminDashboard(studentData, isRefreshing)
        } else {
            InstituteScreenUiState.Guest(isRefreshing)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InstituteScreenUiState.Loading,
        )


    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Handle deep link data first if it exists
                savedStateHandle.get<String>("data")?.let { encodedData ->
                    Log.d(TAG, "data: $encodedData")
                    try {
                        val json = Uri.decode(encodedData)
                        val user = JSONObject(json)
                        val telegramId = user.getLong("id")
                        Log.d("StudyViewModel", "telegram id: $telegramId")

                        val uid = getStudentAuthDataUseCase()?.userId ?: ""
                        if (uid.isNotEmpty()) {
                            storeStudentDataUseCase(uid)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing deep link", e)
                    }
                }

                // Sync user data status
                performSync()
            } finally {
                _isRefreshing.value = false
                _isInitialCheckDone.value = true
            }
        }
    }

    fun onRefreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                performSync()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing student data", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun performSync() {
        try {
            val uid = getStudentAuthDataUseCase()?.userId ?: ""
            if (uid.isEmpty()) return

            val stu = getStudentDataUseCase().first()
            val telegramId = stu?.telegramId ?: 0L

            if (telegramId != 0L) {
                Log.d(TAG, "performSync: telegramId: $telegramId uid: $uid")
                checkStudentMembership(uid, telegramId)
            }
            storeStudentDataUseCase(uid)
        } catch (e: Exception) {
            Log.e(TAG, "performSync error", e)
        }
    }
}
