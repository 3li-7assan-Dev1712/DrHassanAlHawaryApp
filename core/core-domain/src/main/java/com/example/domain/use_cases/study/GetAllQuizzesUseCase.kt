package com.example.domain.use_cases.study

import com.example.domain.module.Quiz
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class GetAllQuizzesUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): List<Quiz> {
        return studyRepository.getAllQuizzes()
    }
}