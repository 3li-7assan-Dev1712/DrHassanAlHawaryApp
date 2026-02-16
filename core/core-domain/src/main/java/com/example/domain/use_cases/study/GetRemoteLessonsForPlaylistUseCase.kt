package com.example.domain.use_cases.study

import com.example.domain.module.Lesson
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class GetRemoteLessonsForPlaylistUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    suspend operator fun invoke(playlistId: String): List<Lesson> {
        return studyRepository.getRemoteLessonsForPlaylist(playlistId)
    }
}