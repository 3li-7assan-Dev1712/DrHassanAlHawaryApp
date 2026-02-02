package com.example.study.domain.use_case

import com.example.study.domain.repository.StudyRepository
import javax.inject.Inject

class SyncLessonsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke() {
        return studyRepository.syncLessons()
    }
}