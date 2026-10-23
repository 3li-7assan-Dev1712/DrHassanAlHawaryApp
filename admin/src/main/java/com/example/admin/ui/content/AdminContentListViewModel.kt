package app.netlify.devalihassan.admin.ui.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.ui.components.AdminActionViewModel
import app.netlify.devalihassan.admin.ui.series.contentType
import com.example.domain.module.ContentCategory
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.module.SeriesPlacement
import com.example.domain.use_cases.categories.GetCategoriesUseCase
import com.example.domain.use_cases.content.UpdateItemCategoryUseCase
import com.example.domain.use_cases.content.UpdateItemPlacementUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What every admin content list injects, in one parameter for the subclasses. */
class ContentListDependencies @Inject constructor(
    val getCategories: GetCategoriesUseCase,
    val getSeries: GetSeriesUseCase,
    val syncSeries: SyncSeriesUseCase,
    val updateCategory: UpdateItemCategoryUseCase,
    val updatePlacement: UpdateItemPlacementUseCase,
)

/** One shown row: the item, its source ([value], for the screen's own actions) and labels. */
data class ContentRow<T>(
    val value: T,
    val item: AdminContentItem,
    val categoryTitle: String?,
    val seriesTitle: String?,
)

data class ContentListState<T>(
    val isLoading: Boolean = true,
    val error: String? = null,
    val rows: List<ContentRow<T>> = emptyList(),
    val total: Int = 0,
    val filter: ContentFilter = ContentFilter(),
    val categories: List<ContentCategory> = emptyList(),
    /** The series offered by the series filter: the chosen category's, or all of them. */
    val series: List<Series> = emptyList(),
    val categoryCounts: Map<CategoryFilter, Int> = emptyMap(),
    val seriesCounts: Map<SeriesFilter, Int> = emptyMap(),
    val selection: Set<String> = emptySet(),
) {
    val hasCategories: Boolean get() = categories.isNotEmpty()
    /** A series shown in episode order, without a search: its episodes can be moved. */
    val canReorder: Boolean get() = filter.series is SeriesFilter.Of && filter.query.isBlank()
}

/**
 * An admin list of one collection: everything on the server, then filtered here (title
 * search, category, series), so any item is a few taps away. Selected items can be moved to
 * a category or a series, or out of their series, in one go; a series' episodes can be
 * moved up and down. Every change goes through updateContent and the list reloads.
 * The route's `categoryId` / `seriesId` arguments open it filtered (see [ContentFilter.fromArgs]).
 */
abstract class AdminContentListViewModel<T>(
    val collection: SeriesCollection,
    savedStateHandle: SavedStateHandle,
    private val deps: ContentListDependencies,
) : AdminActionViewModel() {

    protected abstract suspend fun fetchAll(): List<T>
    protected abstract fun itemOf(value: T): AdminContentItem

    private val loaded = MutableStateFlow<Result<List<T>>?>(null)
    private val filter = MutableStateFlow(
        ContentFilter.fromArgs(savedStateHandle.get<String>(CATEGORY_ARG), savedStateHandle.get<String>(SERIES_ARG))
    )
    private val selection = MutableStateFlow<Set<String>>(emptySet())

    private val categories = (collection.contentType?.let { deps.getCategories(it) } ?: flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val allSeries = deps.getSeries(collection, includeEmpty = true)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val state: StateFlow<ContentListState<T>> = combine(loaded, filter, categories, allSeries, selection) { loaded, filter, categories, series, selection ->
        build(loaded, filter, categories, series, selection)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContentListState())

    init {
        viewModelScope.launch { runCatching { deps.syncSeries() } }
    }

    private var loadJob: Job? = null

    /** Reads everything again; a load still running (a rotation, a quick return) is replaced. */
    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = runCatching { fetchAll() }
            if (result.exceptionOrNull() is CancellationException) return@launch
            loaded.value = result
        }
    }

    fun setQuery(query: String) = filter.update { it.copy(query = query) }

    /** A category the chosen series isn't in clears the series filter. */
    fun setCategory(category: CategoryFilter) = filter.update { current ->
        val series = current.series
        val keepSeries = series !is SeriesFilter.Of || category !is CategoryFilter.Of ||
            allSeries.value.find { it.id == series.seriesId }?.categoryId == category.categoryId
        current.copy(category = category, series = if (keepSeries) series else SeriesFilter.All)
    }

    fun setSeries(series: SeriesFilter) = filter.update { it.copy(series = series) }

    fun toggleSelection(id: String) = selection.update { if (id in it) it - id else it + id }
    fun selectAllShown() = selection.update { it + state.value.rows.map { row -> row.item.id } }
    fun clearSelection() = selection.update { emptySet() }

    /**
     * Moves the selected items to [categoryId]. An item in a series of another category
     * leaves it (as in the editor): the app would show it under the old category's series.
     */
    fun moveSelectedToCategory(categoryId: String) {
        val chosen = selectedItems()
        val seriesCategory = allSeries.value.associate { it.id to it.categoryId }
        bulk {
            for (item in chosen) {
                if (item.categoryId != categoryId) {
                    deps.updateCategory(collection, item.id, categoryId).onFailure { return@bulk Result.failure(it) }
                }
                if (item.seriesId != null && seriesCategory[item.seriesId] != categoryId) {
                    deps.updatePlacement(collection, item.id, SeriesPlacement.Standalone).onFailure { return@bulk Result.failure(it) }
                }
            }
            Result.success(Unit)
        }
    }

    /**
     * Adds the selected items at the end of [series], the oldest first, numbered after its
     * last episode. Audios and videos move to the series' category too.
     */
    fun addSelectedToSeries(series: Series) {
        val all = allItems()
        val chosen = selectedItems().filter { it.seriesId != series.id }.sortedBy { it.publishDate }
        var next = (all.filter { it.seriesId == series.id }.maxOfOrNull { it.episodeNumber ?: 0L } ?: 0L) + 1
        bulk {
            for (item in chosen) {
                val categoryId = series.categoryId
                if (collection.contentType != null && categoryId != null && item.categoryId != categoryId) {
                    deps.updateCategory(collection, item.id, categoryId).onFailure { return@bulk Result.failure(it) }
                }
                deps.updatePlacement(collection, item.id, SeriesPlacement(series.id, next++)).onFailure { return@bulk Result.failure(it) }
            }
            Result.success(Unit)
        }
    }

    /** Makes the selected items standalone (the app's «متفرقات»). */
    fun removeSelectedFromSeries() {
        val chosen = selectedItems().filter { it.seriesId != null }
        bulk {
            for (item in chosen) {
                deps.updatePlacement(collection, item.id, SeriesPlacement.Standalone).onFailure { return@bulk Result.failure(it) }
            }
            Result.success(Unit)
        }
    }

    /**
     * Moves an episode one place up or down in the shown series, then numbers the series
     * 1..n and saves the episodes whose number changed.
     */
    fun moveEpisode(id: String, up: Boolean) {
        val current = state.value
        val series = current.filter.series as? SeriesFilter.Of ?: return
        val episodes = current.rows.map { it.item }.toMutableList()
        val from = episodes.indexOfFirst { it.id == id }
        val to = if (up) from - 1 else from + 1
        if (from < 0 || to !in episodes.indices) return
        episodes.add(to, episodes.removeAt(from))
        val changed = episodes.mapIndexedNotNull { index, item -> (item to index + 1L).takeIf { item.episodeNumber != index + 1L } }
        runAndReload save@{
            for ((item, number) in changed) {
                deps.updatePlacement(collection, item.id, SeriesPlacement(series.seriesId, number)).onFailure { return@save Result.failure<Unit>(it) }
            }
            Result.success(Unit)
        }
    }

    /** After [delete]-style actions of the subclasses. */
    protected fun runAndReload(action: suspend () -> Result<*>) = runAction(action) { load() }

    private fun bulk(action: suspend () -> Result<Unit>) = runAction(action) {
        clearSelection()
        load()
    }

    private fun allItems(): List<AdminContentItem> = loaded.value?.getOrNull().orEmpty().map(::itemOf)
    private fun selectedItems(): List<AdminContentItem> = selection.value.let { ids -> allItems().filter { it.id in ids } }

    private fun build(
        loaded: Result<List<T>>?,
        filter: ContentFilter,
        categories: List<ContentCategory>,
        allSeries: List<Series>,
        selection: Set<String>,
    ): ContentListState<T> {
        val values = loaded?.getOrNull()
        val base = ContentListState<T>(
            isLoading = loaded == null,
            error = loaded?.exceptionOrNull()?.let { it.message ?: "Failed to load" },
            filter = filter,
            categories = categories,
            selection = selection,
        )
        if (values == null) return base

        val byId = values.associateBy { itemOf(it).id }
        val items = values.map(::itemOf)
        val matcher = ContentMatcher(categories.map { it.id }.toSet(), allSeries.map { it.id }.toSet())
        val seriesTitles = allSeries.associate { it.id to it.title }
        val categoryTitles = categories.associate { it.id to it.title }
        val titleMatch = ContentMatcher.titleMatcher(filter.query)
        val searched = items.filter { titleMatch(it.title) }

        // Counts follow the search and the other filter, so each chip says what it would show.
        val forCategories = searched.filter { matcher.matches(it, filter.series) }
        val categoryCounts = buildMap {
            put(CategoryFilter.All, forCategories.size)
            categories.forEach { category -> put(CategoryFilter.Of(category.id), forCategories.count { it.categoryId == category.id }) }
            put(CategoryFilter.None, forCategories.count { !matcher.hasCategory(it) })
        }
        val offeredSeries = (filter.category as? CategoryFilter.Of)
            ?.let { category -> allSeries.filter { it.categoryId == category.categoryId } } ?: allSeries
        val forSeries = searched.filter { matcher.matches(it, filter.category) }
        val seriesCounts = buildMap {
            put(SeriesFilter.All, forSeries.size)
            put(SeriesFilter.None, forSeries.count { it.seriesId == null })
            put(SeriesFilter.Missing, forSeries.count { matcher.matches(it, SeriesFilter.Missing) })
            offeredSeries.forEach { series -> put(SeriesFilter.Of(series.id), forSeries.count { it.seriesId == series.id }) }
        }

        val rows = matcher.apply(items, filter).map { item ->
            ContentRow(
                value = byId.getValue(item.id),
                item = item,
                categoryTitle = item.categoryId?.let(categoryTitles::get),
                seriesTitle = item.seriesId?.let(seriesTitles::get),
            )
        }
        return base.copy(
            rows = rows,
            total = items.size,
            series = offeredSeries,
            categoryCounts = categoryCounts,
            seriesCounts = seriesCounts,
            selection = selection.intersect(byId.keys),
        )
    }

    companion object {
        const val CATEGORY_ARG = "categoryId"
        const val SERIES_ARG = "seriesId"
    }
}
