package com.example.feature.auth.presentation

/** Everything the sign-in screen can show; the screen itself holds no state. */
sealed interface AuthUiState {
    data object Idle : AuthUiState

    /** Google's account picker / Firebase exchange in flight. The button is disabled. */
    data object Loading : AuthUiState

    data object Success : AuthUiState

    /** No Google account on the device: offer to add one. */
    data object NoAccount : AuthUiState

    /** Network or other failure: offer a retry. */
    data object Error : AuthUiState
}
