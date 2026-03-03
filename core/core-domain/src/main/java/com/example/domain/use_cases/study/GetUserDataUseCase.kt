package com.example.domain.use_cases.study

import com.example.domain.module.User
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    operator fun invoke(): Flow<User?> {
        return studyRepository.observeUser()
    }
}
