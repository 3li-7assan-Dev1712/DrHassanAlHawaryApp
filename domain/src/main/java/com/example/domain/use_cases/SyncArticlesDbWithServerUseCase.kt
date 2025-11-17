package com.example.domain.use_cases

import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject

class SyncArticlesDbWithServerUseCase @Inject constructor(
    private val articlesRepository: ArticlesRepository
) {


    suspend operator fun invoke() {
         articlesRepository.syncArticlesDbWithServer()
    }


}