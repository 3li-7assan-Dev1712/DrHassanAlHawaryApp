package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.data_local.model.AudioEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AudioDao {

    /** The paged lists show standalone audios only: a series' audios are reached through its card. */
    @Query("""
        SELECT * FROM audios
        WHERE isDeleted = 0 
        AND seriesId IS NULL
        AND (:query = '' OR title LIKE '%' || :query || '%')
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY publishDate DESC
    """)
    fun getAudiosPagingSource(query: String, categoryId: String?): PagingSource<Int, AudioEntity>

    @Query("SELECT * FROM audios WHERE seriesId = :seriesId AND isDeleted = 0 ORDER BY episodeNumber ASC")
    fun getSeriesAudios(seriesId: String): Flow<List<AudioEntity>>

    /** Audios cached as part of [seriesId] that the server no longer lists in it become standalone. */
    @Query("UPDATE audios SET seriesId = NULL, episodeNumber = NULL WHERE seriesId = :seriesId AND id NOT IN (:keepIds)")
    suspend fun leaveSeries(seriesId: String, keepIds: List<String>)

    @Query("SELECT * FROM audios WHERE isDeleted = 0 ORDER BY title ASC")
    fun getAudiosFlow(): Flow<List<AudioEntity>>

    @Upsert
    suspend fun upsertAll(audios: List<AudioEntity>)

    @Query("DELETE FROM audios")
    suspend fun clearAll()

    @Query("DELETE FROM audios WHERE id = :audioId")
    suspend fun deleteById(audioId: String)

    @Query("SELECT * FROM audios WHERE id IN (:serverAudioIds)")
    fun getAudiosByIds(serverAudioIds: List<String>): List<AudioEntity>

    /** Which of [ids] are cached. */
    @Query("SELECT id FROM audios WHERE id IN (:ids)")
    suspend fun getCachedIds(ids: List<String>): List<String>

    /**
     * Server rows into the cache, keeping what only this device knows about each audio:
     * favourite, download and last played position.
     */
    @Transaction
    suspend fun upsertKeepingUserData(serverAudios: List<AudioEntity>) {
        val localAudios = getAudiosByIds(serverAudios.map { it.id }).associateBy { it.id }
        upsertAll(serverAudios.map { serverAudio ->
            localAudios[serverAudio.id]?.let { localAudio ->
                serverAudio.copy(
                    isFavorite = localAudio.isFavorite,
                    lastPlayedTimestamp = localAudio.lastPlayedTimestamp,
                    localFilePath = localAudio.localFilePath,
                    isDownloaded = localAudio.isDownloaded
                )
            } ?: serverAudio
        })
    }

    @Query("SELECT * FROM audios WHERE audioUrl = :url LIMIT 1")
    fun getAudioByUrl(url: String): Flow<AudioEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM audios WHERE isDeleted = 0 LIMIT 1)")
    suspend fun isCacheEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM audios WHERE isDeleted = 0")
    suspend fun count(): Int


    @Query("SELECT * FROM audios WHERE isDeleted = 0 ORDER BY publishDate DESC LIMIT 1")
    suspend fun getLastAudio(): AudioEntity?

    @Query("SELECT * FROM audios WHERE isDeleted = 0 ORDER BY publishDate DESC LIMIT 5")
    fun getLatestAudios(): Flow<List<AudioEntity>>

}
