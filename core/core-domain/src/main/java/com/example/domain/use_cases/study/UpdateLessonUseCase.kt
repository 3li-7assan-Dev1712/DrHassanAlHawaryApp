package com.example.domain.use_cases.study

import com.example.domain.module.Lesson
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class UpdateLessonUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(
        lesson: Lesson,
        order: Int,
        localAudioUrl: String?,
        localPdfUrl: String?

    ): Result<String> {
        return studyRepository.updateLesson(
            lesson,
            order,
            localAudioUrl,
            localPdfUrl
        )

    }
}