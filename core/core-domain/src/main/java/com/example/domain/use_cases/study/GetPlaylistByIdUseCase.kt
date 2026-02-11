package com.example.domain.use_cases.study

import com.example.domain.module.Playlist
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class GetPlaylistByIdUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(playlistId: String): Playlist?{
        return studyRepository.getRemotePlaylistById(playlistId)

    }
}