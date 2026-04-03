package com.example.domain.use_cases.study

import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class DeletePlaylistUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(playlistId: String): Result<Unit> {
        return studyRepository.deletePlaylist(playlistId)
    }
}
