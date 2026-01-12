package com.example.feature.article.di

import com.example.data.ArticleRepositoryImpl
import com.example.feature.article.domain.repository.ArticleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class ArticleModule {


    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        articleRepository: ArticleRepositoryImpl
    ): ArticleRepository


}