package com.example.domain.use_cases.study

import com.example.domain.module.QuizSubmissionResult
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class SubmitQuizAndPromoteUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(quizId: String, answers: List<Any>): Result<QuizSubmissionResult> {
        return repository.submitQuizAndPromote(quizId, answers)
    }
}
