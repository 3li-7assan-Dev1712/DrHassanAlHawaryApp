package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class PromoteStudentUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(uid: String, nextLevelId: String): Result<Unit> {
        return repository.updateStudentLevel(uid, nextLevelId)
    }
}
