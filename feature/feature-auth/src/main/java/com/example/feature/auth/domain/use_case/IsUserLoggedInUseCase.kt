package com.example.feature.auth.domain.use_case


import com.example.domain.repository.AuthRepository
import javax.inject.Inject


class IsUserLoggedInUseCase @Inject constructor(

    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
    ): Boolean {
//        return authRepository.getLoggedInUser() != null
        return false
    }

}