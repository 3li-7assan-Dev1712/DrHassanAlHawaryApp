package com.example.study.domain.use_case

import com.example.domain.module.Playlist
import com.example.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsForLevelUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    operator fun invoke(levelId: String): Flow<List<Playlist>?> {
        return studyRepository.getPlaylistsForLevel(levelId)
    }
}