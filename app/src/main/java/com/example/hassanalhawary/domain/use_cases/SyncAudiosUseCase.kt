package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.repository.AudiosRepository
import javax.inject.Inject


class SyncAudiosUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository

) {


    suspend operator fun invoke() {
        return audiosRepository.syncAudios()

    }




}