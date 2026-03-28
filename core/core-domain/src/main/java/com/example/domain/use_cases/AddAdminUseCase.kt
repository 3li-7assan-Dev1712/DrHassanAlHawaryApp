package com.example.domain.use_cases

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class AddAdminUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(email: String, role: String): Result<Unit> {
        return studyRepository.addAdmin(email, role)
    }
}
