package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class StoreAdminDataUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(telegramId: Long): Unit {
        studyRepository.storeAdminDataToRoom(telegramId)


    }
}