package com.example.feature.audio.di

import com.example.feature.audio.data.AudioRepositoryImpl
import com.example.feature.audio.domain.repository.AudioRepository
import com.example.feature.audio.player.AudioPlayerManager
import com.example.feature.audio.player.AudioPlayerManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VideoModule {


    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepository: AudioRepositoryImpl
    ): AudioRepository


    @Binds
    @Singleton
    abstract fun bindAudioPlayerManager(
        audioPlayerManagerImpl: AudioPlayerManagerImpl
    ): AudioPlayerManager


}