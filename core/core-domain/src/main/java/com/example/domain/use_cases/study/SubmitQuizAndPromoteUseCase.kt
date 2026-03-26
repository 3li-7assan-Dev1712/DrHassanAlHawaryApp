package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class SubmitQuizAndPromoteUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(answers: List<Any>): Result<Unit> {
        return repository.submitQuizAndPromote(answers)
    }
}
