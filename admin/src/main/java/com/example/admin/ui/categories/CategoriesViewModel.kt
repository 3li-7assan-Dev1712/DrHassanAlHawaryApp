package app.netlify.devalihassan.admin.ui.categories

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.components.AdminActionViewModel
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.audios.GetAllRemoteAudiosUseCase
import com.example.domain.use_cases.categories.DeleteCategoryUseCase
import com.example.domain.use_cases.categories.GetCategoriesUseCase
import com.example.domain.use_cases.categories.SaveCategoryUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.videos.GetAllRemoteVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val type: ContentType = ContentType.AUDIO,
    val categories: List<ContentCategory> = emptyList(),
    /** Items per category id, from the server; null while counting. */
    val itemCounts: Map<String, Int>? = null,
    /** Items with no category or a deleted one: the app shows them only under «الكل». */
    val uncategorized: Int = 0,
    val seriesCounts: Map<String, Int> = emptyMap(),
    @StringRes val message: Int? = null,
) {
    /** Still the built-in categories: the first edit stores them (CategoryRepository). */
    val isBuiltIn: Boolean get() = categories.isNotEmpty() && categories.all { it.documentId == null }
}

/**
 * The audio and video categories («الأقسام»): add, rename, describe, reorder, delete. A
 * category in use can't be deleted (its items would only show under «الكل»); the counts come
 * from every item on the server, so the admin sees what each change touches.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    getSeriesUseCase: GetSeriesUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getAllRemoteAudiosUseCase: GetAllRemoteAudiosUseCase,
    private val getAllRemoteVideosUseCase: GetAllRemoteVideosUseCase,
) : AdminActionViewModel() {

    private val type = MutableStateFlow(ContentType.AUDIO)
    /** The items' category ids of [type], from the server (null: not read yet). */
    private val itemCategories = MutableStateFlow<List<String?>?>(null)
    private val message = MutableStateFlow<Int?>(null)

    private val categories = type.flatMapLatest { getCategoriesUseCase(it) }
    private val series = type.flatMapLatest { getSeriesUseCase(it.seriesCollection, includeEmpty = true) }

    val uiState: StateFlow<CategoriesUiState> = combine(type, categories, itemCategories, series, message) { type, categories, items, series, message ->
        val known = categories.map { it.id }.toSet()
        CategoriesUiState(
            type = type,
            categories = categories,
            itemCounts = items?.filterNotNull()?.groupingBy { it }?.eachCount(),
            uncategorized = items?.count { it == null || it !in known } ?: 0,
            seriesCounts = series.mapNotNull { it.categoryId }.groupingBy { it }.eachCount(),
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    init {
        countItems()
    }

    fun onTypeSelected(type: ContentType) {
        if (this.type.value == type) return
        this.type.value = type
        countItems()
    }

    /** Recounts on every visit: the admin may have moved items on the list screens. */
    fun countItems() {
        val type = type.value
        itemCategories.value = null
        viewModelScope.launch {
            itemCategories.value = runCatching {
                when (type) {
                    ContentType.VIDEO -> getAllRemoteVideosUseCase().map { it.categoryId?.takeIf(String::isNotBlank) }
                    else -> getAllRemoteAudiosUseCase().map { it.categoryId?.takeIf(String::isNotBlank) }
                }
            }.getOrDefault(emptyList())
        }
    }

    /** A new category, last; its id is made here and never changes (items store it). */
    fun create(title: String, description: String) {
        val state = uiState.value
        val category = ContentCategory(
            id = "c" + System.currentTimeMillis().toString(36),
            title = title.trim(),
            type = state.type,
            description = description.trim().ifBlank { null },
            order = (state.categories.maxOfOrNull { it.order } ?: 0L) + 1,
        )
        save { saveCategoryUseCase(category) }
    }

    fun rename(category: ContentCategory, title: String, description: String) = save {
        saveCategoryUseCase(category.copy(title = title.trim(), description = description.trim().ifBlank { null }))
    }

    /** One place up or down, then the categories numbered 1..n; only the changed ones are saved. */
    fun move(category: ContentCategory, up: Boolean) {
        val list = uiState.value.categories.toMutableList()
        val from = list.indexOfFirst { it.id == category.id }
        val to = if (up) from - 1 else from + 1
        if (from < 0 || to !in list.indices) return
        list.add(to, list.removeAt(from))
        val changed = list.mapIndexedNotNull { index, item -> item.copy(order = index + 1L).takeIf { item.order != index + 1L } }
        save {
            for (item in changed) saveCategoryUseCase(item).onFailure { return@save Result.failure(it) }
            Result.success(Unit)
        }
    }

    fun delete(category: ContentCategory) {
        val state = uiState.value
        when {
            state.categories.size <= 1 -> message.value = R.string.category_last
            (state.itemCounts?.get(category.id) ?: 0) > 0 || (state.seriesCounts[category.id] ?: 0) > 0 ->
                message.value = R.string.category_in_use
            else -> save { deleteCategoryUseCase(category) }
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    private fun save(action: suspend () -> Result<Unit>) = runAction(action) { message.value = R.string.changes_saved }

    private val ContentType.seriesCollection: SeriesCollection
        get() = if (this == ContentType.VIDEO) SeriesCollection.VIDEOS else SeriesCollection.AUDIOS
}
