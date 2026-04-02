package com.example.data_local.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data_local.AppDatabase
import com.example.data_local.AudioDao
import com.example.data_local.ImageGroupRemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN batch TEXT")
            }
        }
        
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "hassan_al_hawary_db"
        )
        .addMigrations(MIGRATION_29_30)
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    @Singleton
    fun provideAudioDao(appDatabase: AppDatabase): AudioDao {
        return appDatabase.audioDao()
    }

    @Provides
    @Singleton
    fun provideArticleDao(appDatabase: AppDatabase) = appDatabase.articleDao()

    @Provides
    @Singleton
    fun provideImageDao(appDatabase: AppDatabase) = appDatabase.imageDao()

    @Provides
    @Singleton
    fun provideVideoDao(appDatabase: AppDatabase) = appDatabase.videoDao()


    @Provides
    @Singleton
    fun provideStudentDao(appDatabase: AppDatabase) = appDatabase.studentDao()

    @Provides
    @Singleton
    fun providePlaylistDao(appDatabase: AppDatabase) = appDatabase.playlistDao()

    @Provides
    @Singleton
    fun provideLessonDao(appDatabase: AppDatabase) = appDatabase.lessonDao()

    @Provides
    @Singleton
    fun provideLevelDao(appDatabase: AppDatabase) = appDatabase.levelDao()


    @Provides
    @Singleton
    fun provideImageGroupRemoteKeysDao(appDatabase: AppDatabase): ImageGroupRemoteKeysDao {
        return appDatabase.imageGroupRemoteKeysDao()
    }
}
