package com.example.study.domain.use_case

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class SyncPlaylistsUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke() {
        return studyRepository.syncPlaylists()
    }
}