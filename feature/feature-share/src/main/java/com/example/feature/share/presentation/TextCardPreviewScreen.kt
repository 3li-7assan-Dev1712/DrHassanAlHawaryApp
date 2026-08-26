package com.example.feature.share.presentation

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.remember
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
import com.example.feature.share.engine.TextCardSpec
import com.example.feature.share.presentation.components.GenerationOverlay
import com.example.feature.share.presentation.components.ShareActionBar
import com.example.feature.share.presentation.components.TextCardPreview

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
        viewModel.shareIntentEvent.collect { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newUri(context.contentResolver, chooserTitle, uri)
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val spec = remember { TextCardSpec.default() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .clip(RoundedCornerShape(24.dp)),
                ) {
                    TextCardPreview(
                        content = uiState.content,
                        spec = spec,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    ErrorCard(message = errorMessage, onRetry = onRetry)
                } else {
                    ShareActionBar(
                        enabled = !isGenerating,
                        onShareClick = onShareClick,
                    )
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
private fun ErrorCard(message: String, onRetry: () -> Unit) {
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
        Row(modifier = Modifier.padding(top = 12.dp)) {
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.share_retry))
            }
        }
    }
}
