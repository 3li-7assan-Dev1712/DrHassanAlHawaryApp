package com.example.domain.use_cases


import com.example.domain.module.Audio
import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetAllAudiosUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository

) {


    suspend operator fun invoke(): Flow<List<Audio>> {
        return audiosRepository.getAudiosFromDb()

    }




}