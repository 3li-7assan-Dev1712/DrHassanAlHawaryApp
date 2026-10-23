package app.netlify.devalihassan.admin.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import app.netlify.devalihassan.admin.ui.components.AdminActionFeedback
import app.netlify.devalihassan.admin.ui.content.ContentFilter
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.text.ArabicNumerals

/**
 * «الأقسام»: the audio and video categories. [onOpenItems] opens a type's items list filtered
 * to a category id, or to [ContentFilter.NONE_ARG] for the items with none.
 */
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = hiltViewModel(),
    onOpenItems: (type: ContentType, categoryId: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    AdminActionFeedback(viewModel)
    // Recount on return: items may have been moved on the list screens meanwhile.
    LaunchedEffect(Unit) { viewModel.countItems() }

    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    var editing by remember { mutableStateOf<ContentCategory?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ContentCategory?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creating = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_category)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "types") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.type == ContentType.AUDIO,
                        onClick = { viewModel.onTypeSelected(ContentType.AUDIO) },
                        label = { Text(stringResource(R.string.categories_audio)) },
                    )
                    FilterChip(
                        selected = state.type == ContentType.VIDEO,
                        onClick = { viewModel.onTypeSelected(ContentType.VIDEO) },
                        label = { Text(stringResource(R.string.categories_video)) },
                    )
                }
            }
            if (state.isBuiltIn) {
                item(key = "builtIn") {
                    Text(
                        text = stringResource(R.string.categories_built_in_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.uncategorized > 0) {
                item(key = "uncategorized") {
                    TextButton(onClick = { onOpenItems(state.type, ContentFilter.NONE_ARG) }) {
                        Text(
                            text = stringResource(R.string.uncategorized_items, ArabicNumerals.digits(state.uncategorized)),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            itemsIndexed(state.categories, key = { _, it -> it.id }) { index, category ->
                CategoryRow(
                    category = category,
                    itemCount = state.itemCounts?.get(category.id) ?: if (state.itemCounts == null) null else 0,
                    seriesCount = state.seriesCounts[category.id] ?: 0,
                    canMoveUp = index > 0,
                    canMoveDown = index < state.categories.lastIndex,
                    onOpen = { onOpenItems(state.type, category.id) },
                    onEdit = { editing = category },
                    onDelete = { deleting = category },
                    onMove = { up -> viewModel.move(category, up) },
                )
            }
        }
    }

    if (creating || editing != null) {
        CategoryDialog(
            existing = editing,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { title, description ->
                editing?.let { viewModel.rename(it, title, description) } ?: viewModel.create(title, description)
                creating = false
                editing = null
            },
        )
    }
    deleting?.let { category ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_category)) },
            text = { Text(stringResource(R.string.delete_category_confirm, category.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(category)
                    deleting = null
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun CategoryRow(
    category: ContentCategory,
    itemCount: Int?,
    seriesCount: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: (up: Boolean) -> Unit,
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(category.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                category.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = listOfNotNull(
                        itemCount?.let { stringResource(R.string.category_item_count, ArabicNumerals.digits(it)) },
                        stringResource(R.string.category_series_count, ArabicNumerals.digits(seriesCount)),
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = { onMove(true) }, enabled = canMoveUp) {
                Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up))
            }
            IconButton(onClick = { onMove(false) }, enabled = canMoveDown) {
                Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_category))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_category))
            }
        }
    }
}

/** Create or rename a category: its name and a short description. */
@Composable
private fun CategoryDialog(
    existing: ContentCategory?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) R.string.new_category else R.string.edit_category)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.category_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.category_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, description) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
