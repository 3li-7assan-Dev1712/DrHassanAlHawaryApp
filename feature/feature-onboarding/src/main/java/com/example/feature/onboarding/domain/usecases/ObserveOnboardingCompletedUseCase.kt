package com.example.feature.onboarding.domain.usecases

import com.example.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingCompletedUseCase(
    private val repo: OnboardingRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.isOnboardingCompleted()
}