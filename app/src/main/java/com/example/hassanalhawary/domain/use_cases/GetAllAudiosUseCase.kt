package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.model.AudiosResult
import com.example.hassanalhawary.domain.repository.AudiosRepository
import javax.inject.Inject


class GetAllAudiosUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository

) {


    suspend operator fun invoke(): AudiosResult {
        return audiosRepository.getAllAudios()

    }




}