package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.repository.AuthRepository
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult
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