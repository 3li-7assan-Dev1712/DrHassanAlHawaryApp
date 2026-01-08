package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.ImageGroupEntity

@Dao
interface ImageDao {
    @Upsert
    suspend fun upsertImageGroups(imageGroups: List<ImageGroupEntity>)

    /**
     * Retrieves a PagingSource of all image groups from the database, ordered by title.
     * The Paging library will use this to asynchronously load pages of data.
     */
    @Query("SELECT * FROM image_groups ORDER BY publishDate ASC")
    fun pagingSource(): PagingSource<Int, ImageGroupEntity>

    @Query("DELETE FROM image_groups")
    suspend fun clearAll()


/*    @Query("SELECT * FROM image_groups ORDER BY publishDate DESC LIMIT 5")
    fun getLatestImages(): Flow<List<ImageEntity>>*/


    @Query("SELECT COUNT(*) FROM image_groups")
    suspend fun count(): Int


}