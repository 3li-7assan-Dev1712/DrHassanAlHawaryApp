package com.example.study.domain.use_case

import com.example.domain.module.Lesson
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonByIdUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    operator fun invoke(lessonId: String): Flow<Lesson?> {
        return studyRepository.getLessonById(lessonId)
    }
}