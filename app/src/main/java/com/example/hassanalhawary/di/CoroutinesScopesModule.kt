package com.example.hassanalhawary.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

// 2. This module will provide the scope.
@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {

    // 3. This function creates and provides a SINGLETON CoroutineScope.
    @Singleton
    @ApplicationScope // We label our provider with the custom annotation
    @Provides
    fun providesCoroutineScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher // Injecting the I/O dispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
}