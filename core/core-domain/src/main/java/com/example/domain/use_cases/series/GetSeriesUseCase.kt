package com.example.domain.use_cases.series

import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The series to show above [collection]'s list (only [categoryId]'s when given), in display
 * order. Series with no items yet (created before their content is uploaded) are hidden,
 * except with [includeEmpty] (the admin app, which fills them).
 */
class GetSeriesUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    operator fun invoke(
        collection: SeriesCollection,
        categoryId: String? = null,
        includeEmpty: Boolean = false,
    ): Flow<List<Series>> {
        val series = repository.getSeries(collection, categoryId)
        return if (includeEmpty) series else series.map { all -> all.filter { it.itemCount > 0 } }
    }
}
