package com.example.profile.domain.use_case


import com.example.domain.module.UserData
import com.example.profile.domain.repository.ProfileRepository
import javax.inject.Inject


class GetUserDataUseCase @Inject constructor(

    private val profileRepository: ProfileRepository
) {
    operator fun invoke(
    ): UserData? {
        return profileRepository.getCurrentUserData()
    }

}