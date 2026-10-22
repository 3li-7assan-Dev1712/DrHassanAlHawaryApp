package app.netlify.devalihassan.admin.ui.series

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.components.CategoryDropdownField
import com.example.domain.module.ContentType
import com.example.domain.module.FixedCategories
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.text.SeriesHashtag
import com.example.domain.text.SeriesText

/** The series of one collection, by category for audios and videos, with their actions. */
@Composable
fun SeriesListScreen(viewModel: SeriesListViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    // null: no dialog; NEW_SERIES: the create dialog; else the series being edited.
    var editing by remember { mutableStateOf<Series?>(null) }
    var deleting by remember { mutableStateOf<Series?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = NEW_SERIES },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_series)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CollectionChips(selected = state.collection, onSelect = viewModel::onCollectionSelected)
            if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state.series.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_series), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                SeriesSections(
                    collection = state.collection,
                    series = state.series,
                    enabled = !state.isSaving,
                    onEdit = { editing = it },
                    // A series with items is refused at once (the ViewModel says why).
                    onDelete = { if (it.itemCount > 0) viewModel.delete(it) else deleting = it },
                    onMove = viewModel::move,
                )
            }
        }
    }

    editing?.let { target ->
        val existing = target.takeUnless { it === NEW_SERIES }
        SeriesEditDialog(
            collection = state.collection,
            existing = existing,
            onDismiss = { editing = null },
            onSave = { title, hashtag, categoryId ->
                if (existing == null) {
                    viewModel.create(title, hashtag, categoryId)
                } else {
                    viewModel.save(existing.copy(title = title, hashtag = hashtag, categoryId = categoryId))
                }
                editing = null
            },
        )
    }

    deleting?.let { series ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_series)) },
            text = { Text(stringResource(R.string.delete_series_confirm, series.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(series)
                    deleting = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

/** The create dialog's target in [SeriesListScreen]'s state (compared by identity). */
private val NEW_SERIES = Series(id = "", title = "", collection = SeriesCollection.AUDIOS)

@Composable
private fun CollectionChips(selected: SeriesCollection, onSelect: (SeriesCollection) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SeriesCollection.entries.forEach { collection ->
            FilterChip(
                selected = collection == selected,
                onClick = { onSelect(collection) },
                label = { Text(stringResource(collection.labelRes)) },
            )
        }
    }
}

/**
 * The series grouped by category in the categories' order (audios and videos: the app lists
 * them per category, so they are reordered within one), or as one group.
 */
@Composable
private fun SeriesSections(
    collection: SeriesCollection,
    series: List<Series>,
    enabled: Boolean,
    onEdit: (Series) -> Unit,
    onDelete: (Series) -> Unit,
    onMove: (Series, up: Boolean) -> Unit,
) {
    val categories = collection.contentType?.let { FixedCategories.getCategories(it) }.orEmpty()
    val sections = series.groupBy { it.categoryId }.toList()
        .sortedBy { (categoryId, _) -> categories.indexOfFirst { it.id == categoryId }.let { if (it < 0) Int.MAX_VALUE else it } }
    val uncategorised = stringResource(R.string.series_no_category)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sections.forEach { (categoryId, sectionSeries) ->
            if (collection.contentType != null) {
                item(key = "header:$categoryId") {
                    Text(
                        text = categories.find { it.id == categoryId }?.title ?: uncategorised,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            itemsIndexed(sectionSeries, key = { _, it -> it.id }) { index, item ->
                SeriesRow(
                    series = item,
                    enabled = enabled,
                    canMoveUp = index > 0,
                    canMoveDown = index < sectionSeries.lastIndex,
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item) },
                    onMove = { up -> onMove(item, up) },
                )
            }
        }
    }
}

@Composable
private fun SeriesRow(
    series: Series,
    enabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: (up: Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(series.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (series.hashtag.isNotBlank()) {
                    Text(series.hashtag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = if (series.itemCount > 0) SeriesText.meta(series) else stringResource(R.string.series_empty_admin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onMove(true) }, enabled = enabled && canMoveUp) {
                Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up))
            }
            IconButton(onClick = { onMove(false) }, enabled = enabled && canMoveDown) {
                Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down))
            }
            IconButton(onClick = onEdit, enabled = enabled) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_series))
            }
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_series))
            }
        }
    }
}

/**
 * Create or edit a series: title, hashtag and (audios, videos) category. The hashtag follows
 * the title ([SeriesHashtag]) until it is edited by hand; an existing series' only while it is
 * still its title's, so a rename doesn't change a hashtag already in use.
 */
@Composable
private fun SeriesEditDialog(
    collection: SeriesCollection,
    existing: Series?,
    onDismiss: () -> Unit,
    onSave: (title: String, hashtag: String, categoryId: String?) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var hashtag by remember { mutableStateOf(existing?.hashtag.orEmpty()) }
    var hashtagFollowsTitle by remember {
        mutableStateOf(existing == null || existing.hashtag == SeriesHashtag.fromTitle(existing.title))
    }
    var categoryId by remember { mutableStateOf(existing?.categoryId.orEmpty()) }
    val contentType = collection.contentType
    val canSave = title.isNotBlank() && hashtag.isNotBlank() && (contentType == null || categoryId.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) R.string.new_series else R.string.edit_series)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (hashtagFollowsTitle) hashtag = SeriesHashtag.fromTitle(it)
                    },
                    label = { Text(stringResource(R.string.series_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = hashtag,
                    onValueChange = {
                        hashtag = it
                        hashtagFollowsTitle = false
                    },
                    label = { Text(stringResource(R.string.series_hashtag)) },
                    supportingText = { Text(stringResource(R.string.series_hashtag_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (contentType != null) {
                    CategoryDropdownField(
                        contentType = contentType,
                        selectedCategoryId = categoryId,
                        onCategorySelected = { categoryId = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, withHash(hashtag), categoryId.takeIf { contentType != null }) },
                enabled = canSave,
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** A hashtag typed without its "#" gets one. */
private fun withHash(hashtag: String): String = hashtag.trim().let { if (it.startsWith("#")) it else "#$it" }

/** The categories a collection's series are filed under: audios' and videos' only. */
internal val SeriesCollection.contentType: ContentType?
    get() = when (this) {
        SeriesCollection.AUDIOS -> ContentType.AUDIO
        SeriesCollection.VIDEOS -> ContentType.VIDEO
        SeriesCollection.ARTICLES, SeriesCollection.IMAGE_GROUPS -> null
    }

private val SeriesCollection.labelRes: Int
    get() = when (this) {
        SeriesCollection.AUDIOS -> R.string.audios
        SeriesCollection.ARTICLES -> R.string.articles
        SeriesCollection.VIDEOS -> R.string.videos
        SeriesCollection.IMAGE_GROUPS -> R.string.images
    }
