package com.example.data_local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data_local.model.ArticleEntity
import com.example.data_local.model.AudioEntity
import com.example.data_local.model.ImageEntity
import com.example.data_local.model.ImageGroupEntity
import com.example.data_local.model.ImageGroupRemoteKeysEntity
import com.example.data_local.model.LevelEntity
import com.example.data_local.model.PlaylistEntity
import com.example.data_local.model.StudentEntity
import com.example.data_local.model.VideoEntity


@Database(
    entities = [AudioEntity::class,
        ArticleEntity::class,
        ImageEntity::class,
        ImageGroupEntity::class,
        ImageGroupRemoteKeysEntity::class,
        VideoEntity::class,
        StudentEntity::class,
        LevelEntity::class,
        PlaylistEntity::class],

    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun articleDao(): ArticleDao

    abstract fun imageDao(): ImageDao

    abstract fun imageGroupRemoteKeysDao(): ImageGroupRemoteKeysDao

    abstract fun videoDao(): VideoDao

    abstract fun studentDao(): StudentDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun levelDao(): LevelsDao




}