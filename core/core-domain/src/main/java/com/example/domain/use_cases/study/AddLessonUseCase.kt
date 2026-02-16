package com.example.domain.use_cases.study

import com.example.domain.module.Lesson
import com.example.domain.repository.StudyRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddLessonUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(
        lesson: Lesson,
        playlistId: String,
        order: Int
    ): Flow<UploadResult> {
        return studyRepository.addLesson(
            lesson,
            playlistId,
            order
        )

    }
}