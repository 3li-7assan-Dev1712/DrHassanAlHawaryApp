package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    /** The live series of [collection] (only [categoryId]'s when given), in display order. */
    @Query(
        """
        SELECT * FROM series
        WHERE isDeleted = 0
        AND collection = :collection
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY `order` ASC
    """
    )
    fun getSeries(collection: String, categoryId: String?): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :seriesId AND isDeleted = 0")
    fun getSeriesById(seriesId: String): Flow<SeriesEntity?>

    /** Soft-deleted series are kept with isDeleted = 1, so the queries above skip them. */
    @Upsert
    suspend fun upsertAll(series: List<SeriesEntity>)

    @Query("UPDATE series SET isDeleted = 1 WHERE id = :seriesId")
    suspend fun markDeleted(seriesId: String)
}
