package app.netlify.devalihassan.admin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.netlify.devalihassan.admin.R
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.module.SeriesPlacement
import com.example.domain.text.ArabicNumerals
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** An editor's series fields: the chosen series (none: standalone) and its episode number. */
data class SeriesFormState(
    val seriesId: String? = null,
    /** As typed; Arabic-Indic digits are fine. */
    val episodeNumber: String = "",
    /** What the edited item had (an upload: standalone). */
    val loaded: SeriesPlacement = SeriesPlacement.Standalone,
) {
    val placement: SeriesPlacement
        get() = if (seriesId == null) {
            SeriesPlacement.Standalone
        } else {
            SeriesPlacement(seriesId, ArabicNumerals.toWestern(episodeNumber).toLongOrNull())
        }

    /** Standalone, or in a series at episode 1 or later. */
    val isValid: Boolean
        get() = seriesId == null || (placement.episodeNumber ?: 0L) >= 1L

    /**
     * What an edit writes: null while the choice is what was loaded, so the item's series is
     * left as it is (and an item loaded from a stale cache keeps the server's).
     */
    val changedPlacement: SeriesPlacement?
        get() = placement.takeIf { it != loaded }
}

/**
 * The series fields of an editor, kept by its ViewModel ([scope]): [collection]'s series to
 * choose from (empty ones too, they are being filled), synced as the editor opens, and the
 * choice. Choosing a series suggests the next episode, its item count + 1.
 */
class SeriesFormController(
    scope: CoroutineScope,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
    collection: SeriesCollection,
) {
    val options: StateFlow<List<Series>> = getSeriesUseCase(collection, includeEmpty = true)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _state = MutableStateFlow(SeriesFormState())
    val state: StateFlow<SeriesFormState> = _state.asStateFlow()

    init {
        scope.launch { runCatching { syncSeriesUseCase() } }
    }

    /** The edited item's series, as loaded. */
    fun load(seriesId: String?, episodeNumber: Long?) {
        val loaded = if (seriesId == null) SeriesPlacement.Standalone else SeriesPlacement(seriesId, episodeNumber)
        _state.value = SeriesFormState(
            seriesId = loaded.seriesId,
            episodeNumber = loaded.episodeNumber?.toString().orEmpty(),
            loaded = loaded,
        )
    }

    fun select(series: Series?) {
        _state.update { form ->
            form.copy(
                seriesId = series?.id,
                episodeNumber = when {
                    series == null -> ""
                    series.id == form.loaded.seriesId -> form.loaded.episodeNumber?.toString().orEmpty()
                    else -> (series.itemCount + 1).toString()
                },
            )
        }
    }

    fun onEpisodeNumberChange(text: String) {
        _state.update { it.copy(episodeNumber = text.filter(Char::isDigit)) }
    }

    /** An audio or video moved to [categoryId]: a series filed under another category is let go. */
    fun onCategoryChange(categoryId: String) {
        val chosen = options.value.find { it.id == _state.value.seriesId } ?: return
        if (chosen.categoryId != categoryId) select(null)
    }

    /** After an upload: a blank form for the next one. */
    fun reset() {
        _state.value = SeriesFormState()
    }
}

/**
 * The series picker of an editor: "بدون سلسلة" or one of [controller]'s series (only
 * [categoryId]'s when given), then the episode number for a series.
 */
@Composable
fun SeriesPickerField(
    controller: SeriesFormController,
    modifier: Modifier = Modifier,
    categoryId: String? = null,
) {
    val form by controller.state.collectAsState()
    val allSeries by controller.options.collectAsState()
    val options = allSeries.filter { categoryId.isNullOrBlank() || it.categoryId == categoryId }
    SeriesPickerField(
        form = form,
        options = options,
        onSelect = controller::select,
        onEpisodeNumberChange = controller::onEpisodeNumberChange,
        modifier = modifier,
        selectedTitle = allSeries.find { it.id == form.seriesId }?.title,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesPickerField(
    form: SeriesFormState,
    options: List<Series>,
    onSelect: (Series?) -> Unit,
    onEpisodeNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    // The chosen series' title (it may sit outside [options], e.g. under another category).
    selectedTitle: String? = options.find { it.id == form.seriesId }?.title,
) {
    var expanded by remember { mutableStateOf(false) }
    val none = stringResource(R.string.series_none)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = if (form.seriesId == null) none else selectedTitle ?: form.seriesId,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.series_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(none) },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    },
                )
                options.forEach { series ->
                    DropdownMenuItem(
                        text = { Text(series.title) },
                        onClick = {
                            onSelect(series)
                            expanded = false
                        },
                    )
                }
            }
        }

        if (form.seriesId != null) {
            OutlinedTextField(
                value = form.episodeNumber,
                onValueChange = onEpisodeNumberChange,
                label = { Text(stringResource(R.string.episode_number)) },
                isError = !form.isValid,
                supportingText = if (!form.isValid) {
                    { Text(stringResource(R.string.episode_number_required)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
