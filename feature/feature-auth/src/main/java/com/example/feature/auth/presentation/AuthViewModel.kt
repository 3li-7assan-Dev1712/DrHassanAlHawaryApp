package com.example.feature.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.LoginError
import com.example.domain.module.LoginResult
import com.example.domain.use_cases.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    /**
     * [activityContext] must be the Activity: Credential Manager shows its account picker on it.
     * Typed `Any` like the use case, so this stays testable without Android.
     */
    fun loginWithGoogle(activityContext: Any) {
        // Set synchronously so a second tap before the coroutine starts is ignored too.
        if (_state.value == AuthUiState.Loading) return
        _state.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = try {
                loginWithGoogleUseCase(activityContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d("AuthViewModel", "loginWithGoogle: ${e.message}")
                LoginResult(errorMessage = e.message, error = LoginError.Unknown)
            }
            _state.value = result.toUiState()
        }
    }

    /** The user closed the error / no-account message. */
    fun dismissMessage() {
        val current = _state.value
        if (current == AuthUiState.Error || current == AuthUiState.NoAccount) {
            _state.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _state.value = AuthUiState.Idle
    }

    private fun LoginResult.toUiState(): AuthUiState = when {
        data != null -> AuthUiState.Success
        error == LoginError.Cancelled -> AuthUiState.Idle
        error == LoginError.NoAccount -> AuthUiState.NoAccount
        else -> AuthUiState.Error
    }
}
