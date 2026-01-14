package com.example.feature.auth.domain.use_case

import com.example.domain.module.LoginResult
import com.example.domain.repository.AuthRepository

import javax.inject.Inject


class RegisterNewUserWithEmailPasswordUseCase @Inject constructor(

    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        userName: String,
        email: String,
        password: String
    ): LoginResult {
        return authRepository.registerNewUserWithEmailPassword(
            userName,
            email,
            password
        )
    }

}