package app.netlify.devalihassan.admin.ui.series

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.R
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.series.CreateSeriesUseCase
import com.example.domain.use_cases.series.DeleteSeriesUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.domain.use_cases.series.UpdateSeriesUseCase
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

data class SeriesListUiState(
    val collection: SeriesCollection = SeriesCollection.AUDIOS,
    /** The collection's series in display order: deleted ones left out, empty ones kept. */
    val series: List<Series> = emptyList(),
    val isSaving: Boolean = false,
    @StringRes val message: Int? = null,
)

/**
 * The series of one collection at a time: create, rename / re-tag / change category, reorder
 * and delete, each through the content callables. Writes sync the series back, so the list
 * shows them as the server has them.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeriesListViewModel @Inject constructor(
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
    private val createSeriesUseCase: CreateSeriesUseCase,
    private val updateSeriesUseCase: UpdateSeriesUseCase,
    private val deleteSeriesUseCase: DeleteSeriesUseCase,
) : ViewModel() {

    private val collection = MutableStateFlow(SeriesCollection.AUDIOS)
    private val isSaving = MutableStateFlow(false)
    private val message = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<SeriesListUiState> = combine(
        collection,
        collection.flatMapLatest { getSeriesUseCase(it, includeEmpty = true) },
        isSaving,
        message,
    ) { collection, series, saving, message ->
        SeriesListUiState(collection = collection, series = series, isSaving = saving, message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeriesListUiState())

    init {
        viewModelScope.launch { runCatching { syncSeriesUseCase() } }
    }

    fun onCollectionSelected(collection: SeriesCollection) {
        this.collection.value = collection
    }

    /** A new series, last among its category's (articles and designs have none). */
    fun create(title: String, hashtag: String, categoryId: String?) {
        val state = uiState.value
        val lastOrder = state.series.filter { it.categoryId == categoryId }.maxOfOrNull { it.order } ?: 0L
        val series = Series(
            id = "",
            title = title.trim(),
            collection = state.collection,
            categoryId = categoryId,
            hashtag = hashtag.trim(),
            order = lastOrder + 1,
        )
        write { createSeriesUseCase(series) }
    }

    /** Saves a renamed, re-tagged or re-categorised series. */
    fun save(series: Series) = write { updateSeriesUseCase(series.copy(title = series.title.trim(), hashtag = series.hashtag.trim())) }

    /**
     * Moves [series] one place up or down among its category's series (the app shows them per
     * category), then numbers that category's series 1..n and saves the ones that changed.
     */
    fun move(series: Series, up: Boolean) {
        val section = uiState.value.series.filter { it.categoryId == series.categoryId }.toMutableList()
        val from = section.indexOfFirst { it.id == series.id }
        val to = if (up) from - 1 else from + 1
        if (from < 0 || to !in section.indices) return
        section.add(to, section.removeAt(from))
        val changed = section.mapIndexedNotNull { index, item -> item.copy(order = index + 1L).takeIf { item.order != index + 1L } }
        write {
            for (item in changed) {
                updateSeriesUseCase(item).onFailure { return@write Result.failure(it) }
            }
            Result.success(Unit)
        }
    }

    /**
     * Deletes an empty series. A series with items is refused: its items would keep its id, so
     * the app would show them neither in the series (gone) nor among the standalone items.
     */
    fun delete(series: Series) {
        if (series.itemCount > 0) {
            message.value = R.string.series_delete_not_empty
            return
        }
        write { deleteSeriesUseCase(series.id) }
    }

    fun onMessageShown() {
        message.value = null
    }

    private fun write(block: suspend () -> Result<Unit>) {
        if (isSaving.value) return
        viewModelScope.launch {
            isSaving.value = true
            val result = block()
            isSaving.value = false
            message.value = if (result.isSuccess) R.string.series_saved else R.string.series_save_failed
        }
    }
}
