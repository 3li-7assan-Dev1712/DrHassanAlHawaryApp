package com.example.hassanalhawary.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.hassanalhawary.data.local.model.AudioEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AudioDao {


    @Query("SELECT * FROM audios ORDER BY title ASC")
    fun getAudiosFlow(): Flow<List<AudioEntity>>

    @Upsert
    suspend fun saveAudios(audios: List<AudioEntity>)

}