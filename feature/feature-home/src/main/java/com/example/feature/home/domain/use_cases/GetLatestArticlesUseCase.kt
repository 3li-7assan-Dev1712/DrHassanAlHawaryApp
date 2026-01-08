package com.example.feature.home.domain.use_cases


import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetLatestArticlesUseCase
@Inject constructor(
    private val homeRepository: HomeRepository
) {
    operator fun invoke(): Flow<List<ArticleFeed>> {
        return homeRepository.getLatestArticles()
    }
}