package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class CheckStudentChannelMembershipStateUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(
        uid: String,
        telegramId: Long
    ): Result<Unit> {
        return repository.checkMembership(uid, telegramId)
    }
}
