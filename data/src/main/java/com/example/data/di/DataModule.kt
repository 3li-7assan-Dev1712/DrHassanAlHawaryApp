package com.example.data.di

import com.example.data.ArticlesRepositoryImpl
import com.example.data.AudiosRepositoryImpl
import com.example.data.AuthRepositoryImpl
import com.example.data.DataStoreRepositoryImpl
import com.example.data.ImagesRepositoryImpl
import com.example.data.NetworkRepositoryImpl
import com.example.data.StudyRepositoryImpl
import com.example.data.VideoRepositoryImpl
import com.example.data.WisdomRepositoryImpl
import com.example.domain.repository.ArticlesRepository
import com.example.domain.repository.AudiosRepository
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.DataStoreRepository
import com.example.domain.repository.ImagesRepository
import com.example.domain.repository.NetworkRepository
import com.example.domain.repository.StudyRepository
import com.example.domain.repository.VideosRepository
import com.example.domain.repository.WisdomRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {


    @Binds
    @Singleton
    abstract fun bindWisdomRepository(
        wisdomRepositoryImpl: WisdomRepositoryImpl
    ): WisdomRepository

    @Binds
    @Singleton
    abstract fun bindArticlesRepository(impl: ArticlesRepositoryImpl): ArticlesRepository

    @Binds
    @Singleton
    abstract fun bindAudiosRepository(impl: AudiosRepositoryImpl): AudiosRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(impl: NetworkRepositoryImpl): NetworkRepository

    @Binds
    @Singleton
    abstract fun bindImagesRepository(
        imagesRepositoryImpl: ImagesRepositoryImpl
    ): ImagesRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videosRepositoryImpl: VideoRepositoryImpl
    ): VideosRepository

    @Binds
    @Singleton
    abstract fun bindDataStoresRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): DataStoreRepository


    @Binds
    @Singleton
    abstract fun bindStudyRepository(
        studyRepository: StudyRepositoryImpl
    ): StudyRepository

}


@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        // SupervisorJob so that if one child coroutine fails, the others are not cancelled.
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}