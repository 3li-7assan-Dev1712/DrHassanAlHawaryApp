package com.example.hassanalhawary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hassanalhawary.data.local.model.AudioEntity


@Database(entities = [AudioEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun audioDao(): AudioDao

}