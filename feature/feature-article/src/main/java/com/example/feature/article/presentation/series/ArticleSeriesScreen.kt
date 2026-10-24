package com.example.feature.article.presentation.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.SeriesList
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.Article
import com.example.feature.article.presentation.components.ArticleItem
import com.example.feature.article.presentation.detail.ArticleReaderPane
import com.example.feature.article.presentation.detail.ArticleReaderPaneContent
import com.example.feature.article.presentation.detail.ReaderPreviewState
import com.example.feature.article.presentation.list.ArticleSeriesPreviewData
import com.example.feature.article.presentation.list.ArticlesPreviewData

/**
 * A series of articles: its header, then its articles by episode, each numbered, on the
 * article list's cards. Expanded windows put the reader beside them, as on the articles
 * screen: a tap selects the article ([onSelectArticle]; the caller keeps
 * [selectedArticleId], so it survives rotation and resizing) and the first one is shown
 * until something is chosen. Otherwise a tap opens the reader ([onNavigateToArticleDetail]).
 */
@Composable
fun ArticleSeriesScreen(
    onNavigateToArticleDetail: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToShareSelection: (articleId: String) -> Unit = {},
    selectedArticleId: String? = null,
    onSelectArticle: (articleId: String) -> Unit = {},
    viewModel: ArticleSeriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ArticleSeriesAdaptiveContent(
        state = state,
        selectedArticleId = selectedArticleId,
        onSelectArticle = onSelectArticle,
        onNavigateToArticleDetail = onNavigateToArticleDetail,
        onNavigateBack = onNavigateBack,
        readerPane = { articleId -> ArticleReaderPane(articleId, onShare = onNavigateToShareSelection) },
    )
}

/** [ArticleSeriesScreen] without its ViewModels: previews pass the reader pane. */
@Composable
fun ArticleSeriesAdaptiveContent(
    state: SeriesScreenState<Article>,
    selectedArticleId: String?,
    onSelectArticle: (articleId: String) -> Unit,
    onNavigateToArticleDetail: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit,
    readerPane: @Composable (articleId: String) -> Unit,
) {
    if (layoutTokens.isExpanded) {
        // Until one is chosen, the series' first article is open.
        val shownId = selectedArticleId ?: state.items.firstOrNull()?.id
        TwoPaneLayout(
            listPane = {
                ArticleSeriesList(
                    state = state,
                    onArticleClick = onSelectArticle,
                    onNavigateBack = onNavigateBack,
                    selectedArticleId = shownId,
                )
            },
            detailPane = { if (shownId != null) readerPane(shownId) },
        )
    } else {
        ArticleSeriesList(state, onNavigateToArticleDetail, onNavigateBack)
    }
}

@Composable
private fun ArticleSeriesList(
    state: SeriesScreenState<Article>,
    onArticleClick: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit,
    selectedArticleId: String? = null,
) {
    SeriesList(
        state = state,
        onNavigateBack = onNavigateBack,
        key = { it.id },
        episodeNumber = { it.episodeNumber },
        emptyText = stringResource(R.string.series_empty),
        itemSpacing = 12.dp,
    ) { article ->
        ArticleItem(article = article, onClick = { onArticleClick(article.id) }, selected = article.id == selectedArticleId)
    }
}

private val SeriesPreviewState = SeriesScreenState(
    series = ArticleSeriesPreviewData,
    items = ArticlesPreviewData.mapIndexed { index, article ->
        article.copy(seriesId = ArticleSeriesPreviewData.id, episodeNumber = index + 1L)
    },
    isRefreshing = false,
)

@Composable
private fun ArticleSeriesPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme) {
        ArticleSeriesAdaptiveContent(
            state = SeriesPreviewState,
            selectedArticleId = null,
            onSelectArticle = {},
            onNavigateToArticleDetail = {},
            onNavigateBack = {},
            readerPane = { ArticleReaderPaneContent(ReaderPreviewState, fontStep = 1, onFontStepChange = {}, onShare = {}) },
        )
    }
}

@Preview(name = "Article series - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ArticleSeriesCompactLightPreview() = ArticleSeriesPreview(darkTheme = false)

@Preview(name = "Article series - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ArticleSeriesCompactDarkPreview() = ArticleSeriesPreview(darkTheme = true)

@Preview(name = "Article series - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ArticleSeriesMediumLightPreview() = ArticleSeriesPreview(darkTheme = false)

@Preview(name = "Article series - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ArticleSeriesMediumDarkPreview() = ArticleSeriesPreview(darkTheme = true)

@Preview(name = "Article series + reader - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ArticleSeriesExpandedLightPreview() = ArticleSeriesPreview(darkTheme = false)

@Preview(name = "Article series + reader - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ArticleSeriesExpandedDarkPreview() = ArticleSeriesPreview(darkTheme = true)
