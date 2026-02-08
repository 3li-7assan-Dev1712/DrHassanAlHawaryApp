package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class StoreStudentDataUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(telegramId: Long): Unit {
        studyRepository.saveStudentData(telegramId)


    }
}