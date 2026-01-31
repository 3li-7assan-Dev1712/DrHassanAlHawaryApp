package com.example.study.di

import com.example.study.data.StudyRepositoryImpl
import com.example.study.domain.repository.StudyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StudyModule {


    @Binds
    @Singleton
    abstract fun bindStudyRepository(
        studyRepository: StudyRepositoryImpl
    ): StudyRepository




}
