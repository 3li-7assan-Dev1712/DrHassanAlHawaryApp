package com.example.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.LoginResult
import com.example.domain.use_cases.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthScreenState())
    val state = _state.asStateFlow()

    fun loginWithGoogle() {
        if (_state.value.showSignInProgressBar) return

        viewModelScope.launch {
            showLoading()
            try {
                val result = loginWithGoogleUseCase()
                handleResult(result)
            } catch (e: Exception) {
                handleError(e.message)
            }
        }
    }

    private fun handleResult(result: LoginResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                errorMessage = result.errorMessage,
                showSignInProgressBar = false
            )
        }
    }

    private fun handleError(message: String?) {
        _state.update {
            it.copy(
                errorMessage = message ?: "Something went wrong",
                showSignInProgressBar = false
            )
        }
    }

    private fun showLoading() {
        _state.update {
            it.copy(showSignInProgressBar = true)
        }
    }

    fun resetState() {
        _state.update { AuthScreenState() }
    }
}
