package com.example.feature.onboarding.domain.usecases

import com.example.feature.onboarding.domain.repository.OnboardingRepository

class CompleteOnboardingUseCase(
    private val repo: OnboardingRepository
) {
    suspend operator fun invoke() = repo.setOnboardingCompleted(true)
}