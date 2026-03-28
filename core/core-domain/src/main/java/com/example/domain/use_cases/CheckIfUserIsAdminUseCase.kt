package com.example.domain.use_cases

import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class CheckIfUserIsAdminUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String {
        return authRepository.getUserSecurityRole()
    }
}
