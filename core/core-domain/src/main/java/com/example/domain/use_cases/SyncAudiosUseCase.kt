package com.example.domain.use_cases

import com.example.domain.repository.AudiosRepository
import javax.inject.Inject


class SyncAudiosUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository

) {


    suspend operator fun invoke() {
        return audiosRepository.syncAudiosDbWithServer()

    }




}