package com.example.data_local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.data_local.model.ImageEntity
import com.example.data_local.model.ImageGroupEntity
import com.example.data_local.model.ImageGroupWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Upsert
    suspend fun upsertImageGroups(imageGroups: List<ImageGroupEntity>)

    @Query("SELECT * FROM image_groups WHERE isDeleted = 0 ORDER BY publishDate DESC")
    fun pagingSource(): PagingSource<Int, ImageGroupEntity>

    @Query("DELETE FROM image_groups")
    suspend fun clearAll()

    @Query("DELETE FROM image_groups WHERE id = :groupId")
    suspend fun deleteById(groupId: String)

    @Upsert
    suspend fun upsertImages(images: List<ImageEntity>)

    @Query("SELECT COUNT(*) FROM image_groups WHERE isDeleted = 0")
    suspend fun count(): Int

    @Transaction
    @Query("SELECT * FROM image_groups WHERE id = :groupId AND isDeleted = 0")
    suspend fun getImageGroupWithImages(groupId: String): ImageGroupWithImages?

    @Transaction
    @Query("SELECT * FROM image_groups WHERE isDeleted = 0 ORDER BY publishDate DESC LIMIT 1")
    fun getLastImageGroup(): Flow<ImageGroupWithImages?>

    @Transaction
    @Query("SELECT * FROM image_groups WHERE id = :groupId AND isDeleted = 0")
    fun getObservableGroupWithImages(groupId: String): Flow<ImageGroupWithImages?>

}
