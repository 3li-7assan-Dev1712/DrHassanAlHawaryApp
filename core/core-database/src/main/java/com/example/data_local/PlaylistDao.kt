package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Upsert
    suspend fun upsertAll(playlists: List<PlaylistEntity>)

    @Query(
        """
        SELECT * FROM playlists 
        WHERE levelId = :levelId 
        ORDER BY `order`
    """
    )
    fun getPlaylistsForLevel(levelId: String): Flow<List<PlaylistEntity>?>

    @Query("SELECT MAX(updatedAt) FROM playlists")
    suspend fun lastUpdatedAt(): Long?

    @Query("SELECT id FROM playlists")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM playlists WHERE id = :id")
    fun deletePlaylistById(id: String)
}
