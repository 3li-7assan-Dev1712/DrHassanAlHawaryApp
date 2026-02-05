package com.example.study.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /*@Provides
    @Singleton
    fun provideContentDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.contentDataStore
    }*/
}
