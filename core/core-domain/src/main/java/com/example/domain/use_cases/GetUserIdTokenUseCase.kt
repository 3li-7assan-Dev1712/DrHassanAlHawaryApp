package com.example.domain.use_cases

import com.example.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * A reusable use case to retrieve the Firebase ID Token for the currently logged-in user.
 * This token is typically used for authenticating requests to your own backend server.
 */
class GetUserIdTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String? {
        return authRepository.getLoggedInUser()?.data?.idToken
    }
}
