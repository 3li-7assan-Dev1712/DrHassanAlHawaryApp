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

    /** The paged grid shows standalone groups only: a series' groups are reached through its card. */
    @Query("SELECT * FROM image_groups WHERE isDeleted = 0 AND seriesId IS NULL ORDER BY publishDate DESC")
    fun pagingSource(): PagingSource<Int, ImageGroupEntity>

    @Query("SELECT * FROM image_groups WHERE seriesId = :seriesId AND isDeleted = 0 ORDER BY episodeNumber ASC")
    fun getSeriesImageGroups(seriesId: String): Flow<List<ImageGroupEntity>>

    /** Groups cached as part of [seriesId] that the server no longer lists in it become standalone. */
    @Query("UPDATE image_groups SET seriesId = NULL, episodeNumber = NULL WHERE seriesId = :seriesId AND id NOT IN (:keepIds)")
    suspend fun leaveSeries(seriesId: String, keepIds: List<String>)

    @Query("DELETE FROM image_groups")
    suspend fun clearAll()

    @Query("DELETE FROM image_groups WHERE id = :groupId")
    suspend fun deleteById(groupId: String)

    @Query("DELETE FROM images WHERE group_id = :groupId")
    suspend fun deleteImagesByGroupId(groupId: String)

    @Transaction
    suspend fun deleteGroupWithImages(groupId: String) {
        deleteImagesByGroupId(groupId)
        deleteById(groupId)
    }

    @Upsert
    suspend fun upsertImages(images: List<ImageEntity>)

    @Query("SELECT COUNT(*) FROM image_groups WHERE isDeleted = 0")
    suspend fun count(): Int

    /** What the paged grid can show: groups outside any series. */
    @Query("SELECT COUNT(*) FROM image_groups WHERE isDeleted = 0 AND seriesId IS NULL")
    suspend fun countStandalone(): Int

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
