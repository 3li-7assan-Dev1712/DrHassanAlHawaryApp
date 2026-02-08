package com.example.admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
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
) : ViewModel() {

    val TAG = "MainActivityViewModel"


    private val _state = MutableStateFlow(MainActivityState())

    val state = _state.asStateFlow()

    init {
        checkUserAuthState()
        checkTelegramConnectionState()
    }


    fun checkUserAuthState() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAdminLoggedIn = isUserLoggedInUseCase(),
                )
            }
        }
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

    fun loginSuccess() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAdminLoggedIn = true
                )
            }
        }
    }

    fun logoutSuccess() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAdminLoggedIn = false
                )
            }
        }
    }

}