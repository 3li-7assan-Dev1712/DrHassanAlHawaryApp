package com.example.hassanalhawary.domain.use_cases

import androidx.credentials.Credential
import com.example.hassanalhawary.domain.repository.AuthRepository
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(

    ): LoginResult {
        return authRepository.loginWithGoogle()
    }
}