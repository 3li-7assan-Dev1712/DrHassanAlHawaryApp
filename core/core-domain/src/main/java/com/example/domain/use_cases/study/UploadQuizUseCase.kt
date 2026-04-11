package com.example.domain.use_cases.study

import com.example.domain.module.Quiz
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class UploadQuizUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(quiz: Quiz): Result<Unit> {
        return studyRepository.uploadQuiz(quiz)
    }
}
