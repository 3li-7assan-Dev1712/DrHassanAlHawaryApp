package com.example.domain.use_cases

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class GetAdminsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<List<Map<String, Any>>> {
        return studyRepository.getAdmins()
    }
}
