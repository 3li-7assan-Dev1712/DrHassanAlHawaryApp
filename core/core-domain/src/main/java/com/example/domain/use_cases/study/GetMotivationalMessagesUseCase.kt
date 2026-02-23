package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class GetMotivationalMessagesUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(): List<String> {
        return studyRepository.getRemoteMotivationalMessages()
    }
}