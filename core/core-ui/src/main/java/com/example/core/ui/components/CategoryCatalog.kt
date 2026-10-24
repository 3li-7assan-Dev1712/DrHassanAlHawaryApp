package com.example.core.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.module.FixedCategories
import com.example.domain.module.SeriesCollection

/**
 * The audio and video categories as loaded (renamed and added ones included), for the
 * category labels deep in the UI: video cards, the player's chip, a series' header.
 */
@Immutable
data class CategoryCatalog(
    val audio: List<ContentCategory>,
    val video: List<ContentCategory>,
) {
    /** [categoryId]'s name among [type]'s categories; null when it has none or isn't known. */
    fun title(type: ContentType, categoryId: String?): String? {
        if (categoryId.isNullOrBlank()) return null
        val categories = when (type) {
            ContentType.AUDIO -> audio
            ContentType.VIDEO -> video
            ContentType.ARTICLE -> return null
        }
        return categories.find { it.id == categoryId }?.title
    }

    /** The name of a series' category, by the collection it groups. */
    fun title(collection: SeriesCollection, categoryId: String?): String? = when (collection) {
        SeriesCollection.AUDIOS -> title(ContentType.AUDIO, categoryId)
        SeriesCollection.VIDEOS -> title(ContentType.VIDEO, categoryId)
        SeriesCollection.ARTICLES, SeriesCollection.IMAGE_GROUPS -> null
    }

    companion object {
        /** Before the stored categories load, and in previews. */
        val BuiltIn = CategoryCatalog(
            audio = FixedCategories.getCategories(ContentType.AUDIO),
            video = FixedCategories.getCategories(ContentType.VIDEO),
        )
    }
}

/** Provided by the app's activity from the categories it loads. */
val LocalCategoryCatalog = staticCompositionLocalOf { CategoryCatalog.BuiltIn }
