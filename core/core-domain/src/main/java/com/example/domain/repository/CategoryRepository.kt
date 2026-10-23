package com.example.domain.repository

import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import kotlinx.coroutines.flow.Flow

/**
 * The audio and video categories, editable by the admin: the `categories` collection, or the
 * built-in ones (FixedCategories) while it has none for a type.
 */
interface CategoryRepository {

    /** [type]'s categories in display order; the built-in ones until the server's arrive. */
    fun getCategories(type: ContentType): Flow<List<ContentCategory>>

    /**
     * Creates [category] (no documentId) or saves its title, description and order. The first
     * write of a type stores the built-in categories too, so none of them disappears.
     */
    suspend fun saveCategory(category: ContentCategory): Result<Unit>

    /** Deletes a stored category. The caller makes sure no item uses it any more. */
    suspend fun deleteCategory(category: ContentCategory): Result<Unit>
}
