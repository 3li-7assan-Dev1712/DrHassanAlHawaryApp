package com.example.domain.use_cases

import com.example.domain.module.LoginResult
import com.example.domain.repository.AuthRepository
import javax.inject.Inject


class LoginWithEmailAndPasswordUseCase @Inject constructor(

    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): LoginResult {
        return authRepository.loginWithEmailAndPassword(
            email,
            password
        )
    }

}