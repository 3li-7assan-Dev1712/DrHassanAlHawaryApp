package com.example.hassanalhawary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.IsUserLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainActivityState())

    val state = _state.asStateFlow()

    init {
        checkUserAuthState()
    }

    fun hideProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showProgressBar = false
                )
            }
        }
    }

    fun checkUserAuthState() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUserLoggedIn = isUserLoggedInUseCase()
                )
            }
        }
    }

    fun loginSuccess() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUserLoggedIn = true
                )
            }
        }
    }

}