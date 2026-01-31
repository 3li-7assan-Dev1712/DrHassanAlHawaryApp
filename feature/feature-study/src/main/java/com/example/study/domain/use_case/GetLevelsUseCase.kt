package com.example.study.domain.use_case

import com.example.domain.module.Level
import com.example.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLevelsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    operator fun invoke(): Flow<List<Level>?> {
        return studyRepository.getLevels()
    }
}