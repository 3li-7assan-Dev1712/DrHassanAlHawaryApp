package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.AudioEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AudioDao {

    @Query("""
        
        SELECT * FROM audios
        WHERE :query = '' OR title LIKE '%' || :query || '%'
        ORDER BY publishDate DESC
    """)
    fun getAudiosPagingSource(query: String): PagingSource<Int, AudioEntity>

    @Query("SELECT * FROM audios ORDER BY title ASC")
    fun getAudiosFlow(): Flow<List<AudioEntity>>

    @Upsert
    suspend fun upsertAll(audios: List<AudioEntity>)

    @Query("DELETE FROM audios")
    suspend fun clearAll()

    @Query("SELECT * FROM audios WHERE id IN (:serverAudioIds)")
    fun getAudiosByIds(serverAudioIds: List<String>): List<AudioEntity>


    @Query("SELECT EXISTS(SELECT 1 FROM audios LIMIT 1)")
    suspend fun isCacheEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM audios")
    suspend fun count(): Int


}