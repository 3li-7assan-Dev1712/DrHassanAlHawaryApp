package com.example.domain.use_cases.study

import com.example.domain.module.User
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class UpsertUserUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(user: User) {
        repository.upsertUser(user)
    }
}
