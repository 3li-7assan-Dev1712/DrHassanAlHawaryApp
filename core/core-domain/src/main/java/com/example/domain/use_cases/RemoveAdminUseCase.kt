package com.example.domain.use_cases

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class RemoveAdminUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(uid: String): Result<Unit> {
        return studyRepository.removeAdmin(uid)
    }
}
