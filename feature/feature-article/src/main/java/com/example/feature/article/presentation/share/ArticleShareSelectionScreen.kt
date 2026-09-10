package com.example.feature.article.presentation.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.feature.article.presentation.share.components.SelectableQuoteText

/**
 * Lets the user drag-select an excerpt of the article body, then hands the
 * excerpt + article title off to feature-share's quote-card preview/share
 * screen (wired by the app module, same pattern as AudioDetailScreen's
 * onNavigateToShare -> SharePreviewScreen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleShareSelectionScreen(
    onNavigateUp: () -> Unit,
    onContinueToPreview: (articleTitle: String, excerpt: String) -> Unit,
    viewModel: ArticleShareSelectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArticleShareSelectionScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onSelectionChanged = viewModel::onSelectionChanged,
        onClearSelection = viewModel::onClearSelection,
        onContinue = { onContinueToPreview(uiState.articleTitle, uiState.selectedText) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleShareSelectionScreen(
    uiState: ArticleShareSelectionUiState,
    onNavigateUp: () -> Unit,
    onSelectionChanged: (start: Int, end: Int) -> Unit,
    onClearSelection: () -> Unit,
    onContinue: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.share_text_selection_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            )
        },
        bottomBar = {
            SelectionBottomBar(
                uiState = uiState,
                onClearSelection = onClearSelection,
                onContinue = onContinue,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.displayText.isBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.share_text_selection_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            SelectableQuoteText(
                                text = uiState.displayText,
                                selectionStart = uiState.selectionStart,
                                selectionEnd = uiState.selectionEnd,
                                onSelectionChanged = onSelectionChanged,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBottomBar(
    uiState: ArticleShareSelectionUiState,
    onClearSelection: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    !uiState.hasSelection -> stringResource(R.string.share_text_no_selection)
                    uiState.isTooLong -> stringResource(R.string.share_text_too_long)
                    else -> stringResource(
                        R.string.share_text_char_count,
                        uiState.selectionLength,
                        ArticleShareSelectionUiState.MAX_EXCERPT_LENGTH,
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (uiState.isTooLong) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (uiState.hasSelection) {
                TextButton(onClick = onClearSelection) {
                    Text(stringResource(R.string.share_text_clear_selection))
                }
            }
        }
        Button(
            onClick = onContinue,
            enabled = uiState.canContinue,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(56.dp),
        ) {
            Text(stringResource(R.string.share_text_continue))
        }
    }
}
