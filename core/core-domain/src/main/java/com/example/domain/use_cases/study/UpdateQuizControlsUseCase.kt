package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class UpdateQuizControlsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(quizId: String, isActive: Boolean, startAt: Long?, endAt: Long?): Result<Unit> {
        return studyRepository.updateQuizControls(quizId, isActive, startAt, endAt)
    }
}