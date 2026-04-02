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

    private val MIGRATION_29_30 = object : Migration(29, 30) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE students ADD COLUMN batch TEXT")
        }
    }

    private val MIGRATION_30_31 = object : Migration(30, 31) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE articles ADD COLUMN type TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_31_32 = object : Migration(31, 32) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE audios ADD COLUMN type TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_32_33 = object : Migration(32, 33) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE videos ADD COLUMN type TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_33_34 = object : Migration(33, 34) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE image_groups ADD COLUMN type TEXT NOT NULL DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {

        return Room.databaseBuilder(
            context, AppDatabase::class.java, "hassan_al_hawary_db"
        )
        .addMigrations(
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34
        )
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
