package com.example.feature.share.presentation

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.presentation.components.GenerationOverlay
import com.example.feature.share.presentation.components.QuoteCardPreview
import com.example.feature.share.presentation.components.ShareActionBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCardPreviewScreen(
    onNavigateUp: () -> Unit,
    viewModel: TextCardPreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.share_image_chooser)

    LaunchedEffect(viewModel) {
        viewModel.shareIntentEvent.collect { uris ->
            // Page order is preserved end to end: EXTRA_STREAM list order and ClipData
            // item order both follow the pages, and every image carries "١ / ٣" anyway.
            val intent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList<Uri>(uris))
            }.apply {
                type = "image/png"
                clipData = ClipData.newUri(context.contentResolver, chooserTitle, uris.first()).also { clip ->
                    uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        }
    }

    TextCardPreviewScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onShareClick = viewModel::onShareClicked,
        onRetry = viewModel::onRetry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextCardPreviewScreen(
    uiState: TextCardPreviewUiState,
    onNavigateUp: () -> Unit,
    onShareClick: () -> Unit,
    onRetry: () -> Unit,
) {
    val isGenerating = uiState.exportState is ShareExportState.Preparing

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.share_preview_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.share_preview_close),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(48.dp))
                    uiState.pages.isNotEmpty() -> PagesPager(uiState)
                }

                val errorMessage = uiState.errorMessage
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (errorMessage != null) {
                        ErrorCard(message = errorMessage, onRetry = onRetry, canRetry = uiState.pages.isNotEmpty())
                    } else if (uiState.pages.isNotEmpty()) {
                        ShareActionBar(
                            enabled = !isGenerating,
                            onShareClick = onShareClick,
                        )
                    }
                }
            }
        }

        if (isGenerating) {
            GenerationOverlay(
                progress = 0f,
                titleText = stringResource(R.string.share_generating_image),
                indeterminate = true,
            )
        }
    }
}

@Composable
private fun PagesPager(uiState: TextCardPreviewUiState) {
    val pages = uiState.pages
    val pagerState = rememberPagerState { pages.size }

    // Swiping follows the layout direction, so in Arabic page 1 is on the right
    // and the next page comes in from the left - the same way the text reads.
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 56.dp),
        pageSpacing = 16.dp,
        key = { pages[it].index },
    ) { index ->
        QuoteCardPreview(
            page = pages[index],
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
        )
    }

    if (pages.size > 1) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.share_quote_page_of, pagerState.currentPage + 1, pages.size),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.share_quote_multi_hint, pages.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit, canRetry: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        if (canRetry) {
            Row(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.share_retry))
                }
            }
        }
    }
}
