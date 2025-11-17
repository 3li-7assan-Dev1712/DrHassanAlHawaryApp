package com.example.data_local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data_local.model.ArticleEntity
import com.example.data_local.model.AudioEntity


@Database(entities = [AudioEntity::class, ArticleEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun articleDao(): ArticleDao

}