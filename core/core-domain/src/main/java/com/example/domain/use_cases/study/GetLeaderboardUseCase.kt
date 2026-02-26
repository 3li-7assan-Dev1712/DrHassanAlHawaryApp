package com.example.domain.use_cases.study

import com.example.domain.module.LeaderBoard
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val repository: StudyRepository
) {
    operator fun invoke(): Flow<List<LeaderBoard>> {
        return repository.getLeaderboard()
    }
}
