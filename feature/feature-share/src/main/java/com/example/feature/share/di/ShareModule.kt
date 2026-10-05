package com.example.feature.share.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Bindings for the share-audio-as-video engine. Most engine classes
 * (AudioClipExtractor, WaveformAnalyzer, ShareFileStore, ...) are plain
 * @Inject constructor classes and need no explicit binding here.
 */
@Module
@InstallIn(SingletonComponent::class)
object ShareModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
}
