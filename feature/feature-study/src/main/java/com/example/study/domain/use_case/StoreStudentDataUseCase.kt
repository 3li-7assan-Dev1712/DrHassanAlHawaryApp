package com.example.study.domain.use_case

import com.example.study.domain.repository.StudyRepository
import javax.inject.Inject

class StoreStudentDataUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(telegramId: Long): Unit {
        studyRepository.saveStudentData(telegramId)


    }
}