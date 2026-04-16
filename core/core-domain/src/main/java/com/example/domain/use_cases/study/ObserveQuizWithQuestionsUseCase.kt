package com.example.domain.use_cases.study

import com.example.domain.module.Quiz
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveQuizWithQuestionsUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    operator fun invoke(batchId: String): Flow<Quiz?> {
        return repository.observeQuizWithQuestions(batchId)
    }
}
