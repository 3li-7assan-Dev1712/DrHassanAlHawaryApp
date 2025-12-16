package com.example.data_local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_local.model.ImageGroupRemoteKeysEntity


@Dao
interface ImageGroupRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<ImageGroupRemoteKeysEntity>)

    @Query("SELECT * FROM image_group_remote_keys WHERE groupId = :id")
    suspend fun getRemoteKeyByGroupId(id: String): ImageGroupRemoteKeysEntity?

    @Query("DELETE FROM image_group_remote_keys")
    suspend fun clearRemoteKeys()
}