package com.example.data_local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {


//    suspend fun storePlaylists(playlists: List<PlaylistEntity>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

}