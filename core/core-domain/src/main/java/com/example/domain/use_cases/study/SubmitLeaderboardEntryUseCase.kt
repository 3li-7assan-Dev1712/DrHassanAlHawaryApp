package com.example.domain.use_cases.study

import com.example.domain.module.LeaderBoard
import com.example.domain.repository.StudyRepository
import javax.inject.Inject

class SubmitLeaderboardEntryUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    suspend operator fun invoke(entry: LeaderBoard): Result<Unit> {
        return repository.submitLeaderboardEntry(entry)
    }
}
