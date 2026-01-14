package com.example.profile.domain.use_case


import com.example.domain.module.SignOutResult
import com.example.profile.domain.repository.ProfileRepository
import javax.inject.Inject


class SignOutUseCase @Inject constructor(

    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
    ): SignOutResult {
        return profileRepository.signOut()
    }

}