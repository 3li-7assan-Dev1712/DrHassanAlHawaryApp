package com.example.profile.di

import com.example.profile.data.ProfileRepositoryImpl
import com.example.profile.data.SystemActionsImpl
import com.example.profile.domain.model.SystemActions
import com.example.profile.domain.repository.ProfileRepository
import com.example.profile.presentation.about_app.AndroidAppInfoProvider
import com.example.profile.presentation.about_app.AppInfoProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {


    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSystemAction(
        systemActionsImpl: SystemActionsImpl
    ): SystemActions

    @Binds
    @Singleton
    abstract fun bindAppInfoInterface(
        androidAppInfoProvider: AndroidAppInfoProvider
    ): AppInfoProvider


}