package com.example.domain.use_cases.study

import com.example.domain.module.Playlist
import com.example.domain.repository.StudyRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadPlaylistUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(playlist: Playlist): Flow<UploadResult> {
        return studyRepository.uploadPlaylist(playlist)

    }
}