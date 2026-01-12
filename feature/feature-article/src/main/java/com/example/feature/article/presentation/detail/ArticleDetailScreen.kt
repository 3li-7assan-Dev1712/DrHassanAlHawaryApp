package com.example.feature.article.presentation.detail

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Article
import com.example.feature.article.data.util.formatDate
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    viewModel: DetailArticleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                title = {
                    Text(
                        text = (uiState as? DetailArticleUiState.Success)?.article?.title
                            ?: stringResource(R.string.loading),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        overflow = Ellipsis
                    )

                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )

        },
        floatingActionButton = {
            if (uiState is DetailArticleUiState.Success) {
                FloatingActionButton(
                    onClick = {
                        val article = (uiState as DetailArticleUiState.Success).article
                        shareArticle(
                            context,
                            article.title,
                            "Check out this article: ${article.title}"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Filled.Share, "Share article")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is DetailArticleUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is DetailArticleUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is DetailArticleUiState.Success -> {
                    ArticleContent(
                        article = state.article,
                        formatDate = { date -> formatDate(date) }
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleContent(
    article: Article,
    formatDate: (Date) -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Important for scrollable content
            .padding(16.dp)
    ) {
        Text(
            text = "Published: ${formatDate(article.publishDate)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Log.d("Detail Screen", "ArticleContent: full content: ${article.content}")
        // Full Article Content
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 1.8.em
                ),

                textAlign = TextAlign.Justify

            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

private fun shareArticle(context: Context, subject: String, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailArticleScreenPreview_Success() {
    HassanAlHawaryTheme {
        // Create a dummy ViewModel or pass a success state directly for preview
        val previewArticle = Article(
            id = "preview_id",
            title = "Preview Article: A Long Title to Test Ellipsis Handling in the Top App Bar",
            publishDate = ContentDataType.Companion.Date as Date,
            content = "This is the full preview content.\n\nIt can span multiple paragraphs.\n\nAnd should be scrollable if it's long enough. This is more text to ensure that scrolling will be necessary to see all of it. We are making sure that the content is sufficiently long for testing purposes."
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = previewArticle.title,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* ... */ }) {
                    Icon(Icons.Filled.Share, "Share article")
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                ArticleContent(
                    article = previewArticle,
                    formatDate = { date -> formatDate(date) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailArticleScreenPreview_Loading() {
    HassanAlHawaryTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Dr Hassan Article",
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}