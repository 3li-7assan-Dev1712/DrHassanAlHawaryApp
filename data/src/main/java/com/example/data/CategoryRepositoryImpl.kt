package com.example.data

import com.example.data.di.ApplicationScope
import com.example.data_firebase.CategoryFirestoreSource
import com.example.data_firebase.model.CategoryDto
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.module.FixedCategories
import com.example.domain.module.collectionName
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

/**
 * One live read of `categories` shared by every screen. A type with no stored categories
 * (before the admin's first edit, or when the collection can't be read) shows the built-in
 * ones, so the apps never lose their categories.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val source: CategoryFirestoreSource,
    @ApplicationScope appScope: CoroutineScope,
) : CategoryRepository {

    private val stored = source.observeCategories()
        .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    override fun getCategories(type: ContentType): Flow<List<ContentCategory>> = stored
        .map { all -> all.ofType(type).map { it.toDomain(type) }.ifEmpty { FixedCategories.getCategories(type) } }
        // The built-in ones at once while nothing was read yet, not a blank screen.
        .onStart { if (stored.replayCache.isEmpty()) emit(FixedCategories.getCategories(type)) }
        .distinctUntilChanged()

    override suspend fun saveCategory(category: ContentCategory): Result<Unit> {
        val ofType = storedOfType(category.type).getOrElse { return Result.failure(it) }
        if (ofType.isEmpty()) storeBuiltIns(category.type, except = category.id).onFailure { return Result.failure(it) }
        val documentId = category.documentId ?: ofType.find { it.categoryId == category.id }?.id
        return if (documentId == null) {
            source.createCategory(
                categoryId = category.id,
                collection = category.type.collectionName,
                title = category.title,
                description = category.description,
                order = category.order,
            )
        } else {
            source.updateCategory(
                documentId,
                mapOf("title" to category.title, "description" to category.description, "order" to category.order),
            )
        }
    }

    override suspend fun deleteCategory(category: ContentCategory): Result<Unit> {
        val ofType = storedOfType(category.type).getOrElse { return Result.failure(it) }
        // A built-in category not stored yet: storing the others is what deletes it.
        if (ofType.isEmpty()) return storeBuiltIns(category.type, except = category.id)
        val documentId = category.documentId ?: ofType.find { it.categoryId == category.id }?.id
            ?: return Result.success(Unit)
        return source.deleteCategory(documentId)
    }

    private suspend fun storedOfType(type: ContentType): Result<List<CategoryDto>> =
        source.fetchCategories().map { it.ofType(type) }

    /** The first write of a type: the built-in categories become stored ones, in their order. */
    private suspend fun storeBuiltIns(type: ContentType, except: String): Result<Unit> {
        for (builtIn in FixedCategories.getCategories(type).filter { it.id != except }) {
            source.createCategory(
                categoryId = builtIn.id,
                collection = type.collectionName,
                title = builtIn.title,
                description = builtIn.description,
                order = builtIn.order,
            ).onFailure { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    private fun List<CategoryDto>.ofType(type: ContentType): List<CategoryDto> =
        filter { !it.isDeleted && it.collection == type.collectionName && it.categoryId.isNotBlank() }
            .sortedBy { it.order }

    private fun CategoryDto.toDomain(type: ContentType) = ContentCategory(
        id = categoryId,
        title = title,
        type = type,
        description = description,
        order = order,
        documentId = id,
    )
}
