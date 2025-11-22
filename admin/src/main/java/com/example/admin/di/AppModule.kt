package com.example.admin.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {


/*

    @Binds
    @Singleton
    abstract fun bindArticlesRepository(
        articlesRepositoryImpl: ArticlesRepositoryImpl
    ): ArticlesRepository
*/

}