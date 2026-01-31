package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {


    @Upsert
    suspend fun storePlaylists(playlists: List<PlaylistEntity>)


    @Query("SELECT * FROM playlists WHERE levelId = :level")
    fun getPlaylistsForLevel(level: Int): Flow<List<PlaylistEntity>?>


}