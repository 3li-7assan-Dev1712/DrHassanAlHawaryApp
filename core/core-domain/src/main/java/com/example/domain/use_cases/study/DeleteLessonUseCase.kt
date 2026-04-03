package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class DeleteLessonUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(lessonId: String): Result<Unit> {
        return studyRepository.deleteLesson(lessonId)
    }
}
