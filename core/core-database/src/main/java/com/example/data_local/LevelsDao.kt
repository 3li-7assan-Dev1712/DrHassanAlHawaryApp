package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.LevelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelsDao {



    @Upsert
    suspend fun storeLevels(levels: List<LevelEntity>)


    @Query("SELECT * FROM levels ORDER BY `order`")
    fun getLevels(): Flow<List<LevelEntity>?>

    @Query("SELECT COUNT(*) FROM levels")
    suspend fun count(): Int



}