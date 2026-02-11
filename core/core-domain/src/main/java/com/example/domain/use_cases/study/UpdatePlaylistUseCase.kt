package com.example.domain.use_cases.study

import com.example.domain.module.Playlist
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class UpdatePlaylistUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(playlist: Playlist): Result<String> {
        return studyRepository.updatePlaylist(
            newTitle = playlist.title,
            newLevelId = playlist.levelId,
            newOrder = playlist.order,
            newThumbnailLocalOrRemote = playlist.thumbnailUrl,
            playlistId = playlist.id
        )

    }
}