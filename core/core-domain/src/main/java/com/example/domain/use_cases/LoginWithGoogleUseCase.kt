package com.example.domain.use_cases

import com.example.domain.module.LoginResult
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        activityContext: Any
    ): LoginResult {
        return authRepository.loginWithGoogle(activityContext)
    }
}