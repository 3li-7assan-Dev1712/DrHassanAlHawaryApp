package com.example.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.LoginResult
import com.example.domain.use_cases.LoginWithEmailAndPasswordUseCase
import com.example.domain.use_cases.LoginWithGoogleUseCase
import com.example.domain.use_cases.RegisterNewUserWithEmailPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val loginWithEmailAndPasswordUseCase: LoginWithEmailAndPasswordUseCase,
    private val registerNewUserWithEmailPasswordUseCase: RegisterNewUserWithEmailPasswordUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthScreenState())

    val state = _state.asStateFlow()

    fun loginWithGoogle() {

        viewModelScope.launch {
            // show progress
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
            try {
                val loginResult = loginWithGoogleUseCase()
                onSignInResult(loginResult)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        errorMessage = e.message,
                        showSignInProgressBar = false
                    )
                }
            }
        }
    }

    fun loginWithEmailPassword(email: String, password: String) {

        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
            val loginResult = loginWithEmailAndPasswordUseCase(email, password)
            onSignInResult(loginResult)
        }

    }

    fun registerNewUserWithEmailPassword(userName: String, email: String, password: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
            val loginResult = registerNewUserWithEmailPasswordUseCase(userName, email, password)
            onSignInResult(loginResult)
        }
    }

    fun onSignInResult(loginResult: LoginResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = loginResult.data != null,
                errorMessage = loginResult.errorMessage,
                showSignInProgressBar = false
            )
        }
    }

    fun resetState() {
        _state.update {
            AuthScreenState()
        }
    }

    fun userClickSignInBtn() {
        if (_state.value.enteredEmail.isBlank()) {
            _state.update {
                it.copy(
                    enterValidEmailMsg =
                        "Please enter a vaild email"
                )
            }
        } else if (_state.value.enteredPassword.isBlank()) {
            _state.update {
                it.copy(
                    enterValidPasswordMsg =
                        "Please enter a valid password"
                )
            }
        } else {
            // show progress bar indicator
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
        }
    }

    fun emailChanged(email: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(enteredEmail = email)
            }
        }

    }

    fun passwordChanged(password: String) {

        viewModelScope.launch {
            _state.update {
                it.copy(enteredPassword = password)
            }
        }
    }

    fun userNameChanged(userName: String) {

        viewModelScope.launch {
            _state.update {
                it.copy(userName = userName)
            }
        }
    }

    fun showProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
        }

    }

    fun hideProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSignInProgressBar = false
                )
            }
        }

    }


}