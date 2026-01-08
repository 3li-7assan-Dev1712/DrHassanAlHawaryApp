package com.example.domain.use_cases.articles

import com.example.domain.module.Article
import com.example.domain.repository.ArticlesRepository
import javax.inject.Inject

class UploadArticleUseCase @Inject constructor(
private val articleRepository: ArticlesRepository
) {

    /**
     * Invokes the use case to upload a new article.
     * article: The article to be uploaded.
     * @throws IllegalArgumentException if title or content is blank.
     *
     * @return Boolean indicating success or failure of the upload.
     */
    suspend operator fun invoke(article: Article){
        // You can add validation here as an extra layer of business logic,
        // though the primary validation is in the ViewModel.
        if (article.title.isBlank() || article.content.isBlank()) {
            throw IllegalArgumentException("Article title and content cannot be empty.")
        }

        articleRepository.uploadArticle(
            article
        )
    }


}