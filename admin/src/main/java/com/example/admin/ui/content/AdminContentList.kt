package app.netlify.devalihassan.admin.ui.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.components.AdminActionFeedback
import com.example.core.ui.animation.LoadingScreen
import com.example.domain.module.ContentCategory
import com.example.domain.text.ArabicNumerals
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * An admin content list: search and filters at the top, then the items. A tap opens an
 * item ([onOpen]); a long press starts selecting, and the bar at the bottom changes the
 * selected items' category or series. Filtered to one series, its episodes get up/down
 * arrows. [leading]: a picture before the title (design groups).
 */
@Composable
fun <T> AdminContentList(
    viewModel: AdminContentListViewModel<T>,
    addLabel: String,
    onAdd: () -> Unit,
    onOpen: (T) -> Unit,
    onDelete: (T) -> Unit,
    leading: (@Composable (T) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    // Reload on every visit so an edit or upload made on the next screen shows up on return.
    LaunchedEffect(Unit) { viewModel.load() }
    AdminActionFeedback(viewModel)

    var toDelete by remember { mutableStateOf<ContentRow<T>?>(null) }
    var pickCategory by remember { mutableStateOf(false) }
    var pickSeries by remember { mutableStateOf(false) }
    val selecting = state.selection.isNotEmpty()

    Scaffold(
        floatingActionButton = {
            if (!selecting) {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = addLabel)
                }
            }
        },
        bottomBar = {
            if (selecting) {
                SelectionBar(
                    count = state.selection.size,
                    hasCategories = state.hasCategories,
                    onSelectAll = viewModel::selectAllShown,
                    onClear = viewModel::clearSelection,
                    onMoveToCategory = { pickCategory = true },
                    onAddToSeries = { pickSeries = true },
                    onRemoveFromSeries = viewModel::removeSelectedFromSeries,
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterHeader(state = state, viewModel = viewModel)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingScreen() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                state.rows.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_matching_items), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(state.rows, key = { _, row -> row.item.id }) { index, row ->
                        ContentRowCard(
                            row = row,
                            showCategory = state.hasCategories,
                            selected = row.item.id in state.selection,
                            selecting = selecting,
                            reorder = state.canReorder,
                            canMoveUp = index > 0,
                            canMoveDown = index < state.rows.lastIndex,
                            onClick = { if (selecting) viewModel.toggleSelection(row.item.id) else onOpen(row.value) },
                            onLongClick = { viewModel.toggleSelection(row.item.id) },
                            onMove = { up -> viewModel.moveEpisode(row.item.id, up) },
                            onDelete = { toDelete = row },
                            leading = leading?.let { content -> { content(row.value) } },
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_confirmation_msg, row.item.title)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(row.value)
                    toDelete = null
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (pickCategory) {
        ChoiceDialog(
            title = stringResource(R.string.move_to_category),
            note = stringResource(R.string.move_to_category_note),
            choices = state.categories,
            label = ContentCategory::title,
            onChoose = { viewModel.moveSelectedToCategory(it.id) },
            onDismiss = { pickCategory = false },
        )
    }
    if (pickSeries) {
        val categoryTitles = state.categories.associate { it.id to it.title }
        ChoiceDialog(
            title = stringResource(R.string.add_to_series),
            note = stringResource(R.string.add_to_series_note),
            choices = state.series,
            label = { series -> listOfNotNull(series.title, series.categoryId?.let(categoryTitles::get)).joinToString(" · ") },
            onChoose = viewModel::addSelectedToSeries,
            onDismiss = { pickSeries = false },
        )
    }
}

/** Search, the category chips (audios, videos) and the series filter, then "١٢ من ٧٨٩". */
@Composable
private fun <T> FilterHeader(state: ContentListState<T>, viewModel: AdminContentListViewModel<T>) {
    Column(Modifier.padding(top = 8.dp)) {
        OutlinedTextField(
            value = state.filter.query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.search_by_title)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.filter.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) }
                }
            },
        )
        if (state.hasCategories) {
            val chips = buildList {
                add(CategoryFilter.All to stringResource(R.string.filter_all))
                state.categories.forEach { add(CategoryFilter.Of(it.id) to it.title) }
                add(CategoryFilter.None to stringResource(R.string.filter_no_category))
            }.filter { (filter, _) ->
                // «بدون قسم» only when there is something to fix (or it is the chosen one).
                filter != CategoryFilter.None || (state.categoryCounts[filter] ?: 0) > 0 || state.filter.category == filter
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                items(chips, key = { it.first.toString() }) { (filter, title) ->
                    FilterChip(
                        selected = state.filter.category == filter,
                        onClick = { viewModel.setCategory(filter) },
                        label = { Text(countLabel(title, state.categoryCounts[filter])) },
                    )
                }
            }
        }
        SeriesFilterField(state = state, onSelect = viewModel::setSeries)
        Text(
            text = stringResource(R.string.shown_of_total, ArabicNumerals.digits(state.rows.size), ArabicNumerals.digits(state.total)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SeriesFilterField(state: ContentListState<T>, onSelect: (SeriesFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = buildList {
        add(SeriesFilter.All to stringResource(R.string.filter_all_series))
        add(SeriesFilter.None to stringResource(R.string.filter_no_series))
        if ((state.seriesCounts[SeriesFilter.Missing] ?: 0) > 0 || state.filter.series == SeriesFilter.Missing) {
            add(SeriesFilter.Missing to stringResource(R.string.filter_missing_series))
        }
        state.series.forEach { add(SeriesFilter.Of(it.id) to it.title) }
    }
    val selected = options.find { it.first == state.filter.series }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
    ) {
        OutlinedTextField(
            value = selected?.let { countLabel(it.second, state.seriesCounts[it.first]) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.series_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (filter, title) ->
                DropdownMenuItem(
                    text = { Text(countLabel(title, state.seriesCounts[filter])) },
                    onClick = {
                        onSelect(filter)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> ContentRowCard(
    row: ContentRow<T>,
    showCategory: Boolean,
    selected: Boolean,
    selecting: Boolean,
    reorder: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMove: (up: Boolean) -> Unit,
    onDelete: () -> Unit,
    leading: (@Composable () -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primaryContainer else colors.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (selecting) {
                Checkbox(checked = selected, onCheckedChange = { onLongClick() })
            } else {
                leading?.invoke()
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = metaLine(row, showCategory),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (showCategory && row.categoryTitle == null) colors.error else colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (reorder && !selecting) {
                IconButton(onClick = { onMove(true) }, enabled = canMoveUp) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up))
                }
                IconButton(onClick = { onMove(false) }, enabled = canMoveDown) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down))
                }
            }
            if (!selecting) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = colors.error)
                }
            }
        }
    }
}

/** "١٢/٠٣/٢٠٢٤ · فتاوى · شرح الحج · الحلقة ٣"; a missing category or series is named as such. */
@Composable
private fun <T> metaLine(row: ContentRow<T>, showCategory: Boolean): String {
    val item = row.item
    val date = remember(item.publishDate) { ArabicNumerals.digits(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(item.publishDate)) }
    val category = if (showCategory) row.categoryTitle ?: stringResource(R.string.filter_no_category) else null
    val series = when {
        item.seriesId == null -> null
        row.seriesTitle == null -> stringResource(R.string.filter_missing_series)
        else -> listOfNotNull(
            row.seriesTitle,
            item.episodeNumber?.let { stringResource(R.string.episode_short, ArabicNumerals.digits(it.toString())) },
        ).joinToString(" · ")
    }
    return listOfNotNull(date, category, series).joinToString(" · ")
}

@Composable
private fun SelectionBar(
    count: Int,
    hasCategories: Boolean,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onMoveToCategory: () -> Unit,
    onAddToSeries: () -> Unit,
    onRemoveFromSeries: () -> Unit,
) {
    Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
        Column(Modifier.fillMaxWidth()) {
            HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.selected_count, ArabicNumerals.digits(count)),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )
                TextButton(onClick = onSelectAll) { Text(stringResource(R.string.select_all_shown)) }
                TextButton(onClick = onClear) { Text(stringResource(R.string.cancel_selection)) }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (hasCategories) TextButton(onClick = onMoveToCategory) { Text(stringResource(R.string.move_to_category)) }
                TextButton(onClick = onAddToSeries) { Text(stringResource(R.string.add_to_series)) }
                TextButton(onClick = onRemoveFromSeries) { Text(stringResource(R.string.remove_from_series)) }
            }
        }
    }
}

/** A short list to pick one of, with a note on what the choice does. */
@Composable
private fun <C> ChoiceDialog(
    title: String,
    note: String,
    choices: List<C>,
    label: (C) -> String,
    onChoose: (C) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.padding(top = 8.dp)) {
                    items(choices) { choice ->
                        TextButton(
                            onClick = {
                                onChoose(choice)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(label(choice), modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private fun countLabel(title: String, count: Int?): String =
    if (count == null) title else "$title (${ArabicNumerals.digits(count)})"
