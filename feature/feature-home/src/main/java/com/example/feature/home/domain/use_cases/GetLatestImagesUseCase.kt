package com.example.feature.home.domain.use_cases


import com.example.feature.home.domain.model.ImageFeed
import com.example.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestImagesUseCase
@Inject constructor(
    private val homeRepository: HomeRepository
) {
    operator fun invoke(): Flow<List<ImageFeed>> {
        return homeRepository.getLatestImages()
    }
}