package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class SyncUserUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(uid: String) {
        val remoteUser = studyRepository.getRemoteUser(uid)
        if (remoteUser != null) {
            studyRepository.upsertUser(remoteUser)
        }
    }
}
