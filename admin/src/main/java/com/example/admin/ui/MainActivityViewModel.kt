package com.example.admin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.GetUserIdTokenUseCase
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.study.DeleteStudentDataUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.StoreStudentDataUseCase
import com.example.profile.domain.use_case.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getStudentDataUseCase: GetStudentDataUseCase,
    private val getCurrentUserDataUseCase: GetUserDataUseCase,
    private val storeStudentDataUseCase: StoreStudentDataUseCase,
    private val getUserIdTokenUseCase: GetUserIdTokenUseCase,
    private val deleteStudentDataUseCase: DeleteStudentDataUseCase,
) : ViewModel() {

    val TAG = "MainActivityViewModel"


    private val _state = MutableStateFlow(MainActivityState())

    val state = _state.asStateFlow()

    init {
        checkUserAuthState()
        checkTelegramConnectionState()
    }


    fun checkTelegramConnectionState() {
        viewModelScope.launch {

            getStudentDataUseCase().collectLatest { adminData ->
                _state.update {
                    it.copy(
                        isAdminConnectedToTelegram = adminData != null
                    )
                }
            }


        }
    }

    fun checkUserAuthState() {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase()
            Log.d(TAG, "checkUserAuthState: isLoggedIn : $isLoggedIn")
            _state.update {
                it.copy(
                    isAdminLoggedIn = isLoggedIn,
                    isLoading = false
                )
            }
            if (isLoggedIn) {
                val idToken = getUserIdTokenUseCase()
                _state.update {
                    it.copy(
                        currentUserDate = getCurrentUserDataUseCase(),
                        idToken = idToken
                    )
                }

            }
        }
    }

    fun loginSuccess() {
        viewModelScope.launch {
            _state.update { it.copy(isAdminLoggedIn = true) }
            val idToken = getUserIdTokenUseCase()
            _state.update { it.copy(idToken = idToken) }
            val uid = getCurrentUserDataUseCase()?.userId
            if (uid != null)
                storeStudentDataUseCase(uid)
        }
    }

    fun logoutSuccess() {
        viewModelScope.launch {
            _state.update { it.copy(isAdminLoggedIn = false) }
            deleteStudentDataUseCase()
        }
    }

}