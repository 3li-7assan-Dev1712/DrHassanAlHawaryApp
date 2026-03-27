package com.example.feature.auth.presentation

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.module.LoginResult
import com.example.domain.repository.AuthRepository
import com.example.domain.use_cases.LoginWithEmailAndPasswordUseCase
import com.example.domain.use_cases.LoginWithGoogleUseCase
import com.example.domain.use_cases.RegisterNewUserWithEmailPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val registerNewUserWithEmailPasswordUseCase: RegisterNewUserWithEmailPasswordUseCase,
    private val authRepository: AuthRepository,
    @ApplicationContext val context: Context
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

    fun loginWithEmailPassword() {
        if (!validateInputs()) return
        if (_state.value.showSignInProgressBar) return

        viewModelScope.launch {
            showLoading()
            try {
                val result = loginWithEmailAndPasswordUseCase(
                    _state.value.enteredEmail.trim(),
                    _state.value.enteredPassword
                )
                if (result.data != null) {
                    authRepository.reloadUser()
                    if (authRepository.isEmailVerified()) {
                        handleResult(result)
                    } else {
                        handleError(context.getString(R.string.email_not_verified))
                    }
                } else {
                    handleResult(result)
                }
            } catch (e: Exception) {
                handleError(e.message)
            }
        }
    }

    fun registerNewUser() {
        if (!validateInputs(isRegister = true)) return
        if (_state.value.showSignInProgressBar) return

        viewModelScope.launch {
            showLoading()
            try {
                val result = registerNewUserWithEmailPasswordUseCase(
                    _state.value.userName.trim(),
                    _state.value.enteredEmail.trim(),
                    _state.value.enteredPassword
                )
                if (result.data != null) {
                    authRepository.sendEmailVerification()
                    _state.update {
                        it.copy(
                            isSignInSuccessful = false,
                            showSignInProgressBar = false,
                            errorMessage = context.getString(R.string.email_verification_msg)
                        )
                    }
                } else {
                    handleResult(result)
                }
            } catch (e: Exception) {
                handleError(e.message)
            }
        }
    }

    fun sendPasswordResetEmail() {
        val email = _state.value.enteredEmail.trim()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(enterValidEmailMsg = context.getString(R.string.enter_valid_email)) }
            return
        }

        viewModelScope.launch {
            showLoading()
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            showSignInProgressBar = false,
                            errorMessage = context.getString(R.string.password_reset_email_sent)
                        )
                    }
                }
                .onFailure { e ->
                    handleError(e.message)
                }
        }
    }


    private fun handleResult(result: LoginResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                errorMessage = result.errorMessage,
                showSignInProgressBar = false,
                enterValidEmailMsg = "",
                enterValidPasswordMsg = ""
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


    fun emailChanged(email: String) {
        _state.update {
            it.copy(
                enteredEmail = email,
                enterValidEmailMsg = ""
            )
        }
    }

    fun passwordChanged(password: String) {
        _state.update {
            it.copy(
                enteredPassword = password,
                enterValidPasswordMsg = ""
            )
        }
    }

    fun userNameChanged(userName: String) {
        _state.update {
            it.copy(userName = userName)
        }
    }


    data class PasswordValidation(
        val hasMinLength: Boolean = false,
        val hasNumber: Boolean = false,
        val hasUpperCase: Boolean = false
    )

    fun getPasswordValidation(password: String): PasswordValidation {
        return PasswordValidation(
            hasMinLength = password.length >= 6,
            hasNumber = password.any { it.isDigit() },
            hasUpperCase = password.any { it.isUpperCase() }
        )
    }


    private fun validateInputs(isRegister: Boolean = false): Boolean {
        val email = _state.value.enteredEmail.trim()
        val password = _state.value.enteredPassword
        val userName = _state.value.userName.trim()

        val passwordValidation = getPasswordValidation(password)

        var emailError = ""
        var passwordError = ""
        var generalError: String? = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email"
        }

        if (!passwordValidation.hasMinLength ||
            !passwordValidation.hasNumber ||
            !passwordValidation.hasUpperCase
        ) {
            passwordError = "Password is too weak"
        }

        if (isRegister && userName.isBlank()) {
            generalError = "Enter your name"
        }

        _state.update {
            it.copy(
                enterValidEmailMsg = emailError,
                enterValidPasswordMsg = passwordError,
                errorMessage = generalError
            )
        }

        return emailError.isEmpty() &&
                passwordError.isEmpty() &&
                generalError == null
    }


}
