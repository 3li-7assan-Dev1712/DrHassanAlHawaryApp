package com.example.domain.use_cases.study

import com.example.domain.module.Quiz
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class GetQuizWithQuestionsUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(batchId: String): Quiz? {
        return repository.getQuizWithQuestions(batchId)
    }
}
