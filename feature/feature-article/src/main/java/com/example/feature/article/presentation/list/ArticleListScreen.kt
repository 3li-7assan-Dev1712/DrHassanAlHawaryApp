package com.example.feature.article.presentation.list

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.Article
import com.example.feature.article.presentation.components.ArticleItem
import com.example.feature.article.presentation.detail.ArticleReaderPane
import com.example.feature.article.presentation.detail.ArticleReaderPaneContent
import com.example.feature.article.presentation.detail.ReaderPreviewState
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date


/**
 * The articles. Expanded windows: the list and the reader side by side; a tap selects the
 * article ([onSelectArticle]; the caller keeps [selectedArticleId], so it survives rotation
 * and resizing) and the newest one is shown until something is chosen. Otherwise the
 * phone's list, where a tap opens the reader ([onNavigateToArticleDetail]).
 */
@Composable
fun ArticleListScreen(
    articlesViewModel: ArticleListViewModel = hiltViewModel(),
    onNavigateToArticleDetail: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToShareSelection: (articleId: String) -> Unit = {},
    selectedArticleId: String? = null,
    onSelectArticle: (articleId: String) -> Unit = {},
) {
    val articles = articlesViewModel.articles.collectAsLazyPagingItems()

    ArticlesAdaptiveContent(
        articles = articles,
        selectedArticleId = selectedArticleId,
        onSelectArticle = onSelectArticle,
        onNavigateToArticleDetail = onNavigateToArticleDetail,
        onNavigateBack = onNavigateBack,
        readerPane = { articleId -> ArticleReaderPane(articleId, onShare = onNavigateToShareSelection) },
    )
}

/** [ArticleListScreen] without its ViewModels: previews and UI tests pass the reader pane. */
@Composable
fun ArticlesAdaptiveContent(
    articles: LazyPagingItems<Article>,
    selectedArticleId: String?,
    onSelectArticle: (articleId: String) -> Unit,
    onNavigateToArticleDetail: (articleId: String) -> Unit,
    onNavigateBack: () -> Unit,
    readerPane: @Composable (articleId: String) -> Unit,
) {
    if (layoutTokens.isExpanded) {
        // Until one is chosen, the newest article (the list's first) is open.
        val shownId = selectedArticleId ?: if (articles.itemCount > 0) articles.peek(0)?.id else null
        TwoPaneLayout(
            listPane = {
                ArticlesScreenContent(
                    articles = articles,
                    onArticleClick = onSelectArticle,
                    onNavigateBack = onNavigateBack,
                    selectedArticleId = shownId,
                )
            },
            detailPane = { if (shownId != null) readerPane(shownId) },
        )
    } else {
        ArticlesScreenContent(articles, onNavigateToArticleDetail, onNavigateBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesScreenContent(
    articles: LazyPagingItems<Article>,
    onArticleClick: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    selectedArticleId: String? = null,
) {

    val listState = rememberLazyListState()

    // Paging's local-cache-first load can insert newer items (the mediator's remote
    // fetch, or a genuinely new article) above whatever the list last anchored on,
    // which otherwise leaves the list looking "scrolled down" without ever having
    // moved. Track the newest item's key and snap/animate back to it when it changes.
    var topItemKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(articles.itemCount) {
        if (articles.itemCount == 0) return@LaunchedEffect
        val newestKey = articles.peek(0)?.id
        if (newestKey != null && newestKey != topItemKey) {
            if (topItemKey == null) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(0)
            }
            topItemKey = newestKey
        }
    }

    Scaffold(
        containerColor = Brand.colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.articles), onBack = onNavigateBack) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            val isInitialLoad = articles.loadState.refresh is LoadState.Loading

            val reduced = reducedMotion
            AnimatedContent(
                targetState = isInitialLoad,
                transitionSpec = { Motion.contentSwap(reduced) },
                label = "articlesContent",
            ) { loading ->
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Brand.colors.accentStrong,
                            strokeWidth = 3.dp
                        )
                    }
                } else {
    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            count = articles.itemCount,
                            key = articles.itemKey { it.id }
                        ) { index ->
                            val art = articles[index]
                            if (art != null) {
                                ArticleItem(
                                    article = art,
                                    onClick = { onArticleClick(art.id) },
                                    selected = art.id == selectedArticleId,
                                )
                            }
                        }
    
                        // when scroll down show loading will append new arts
                        if (articles.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp,
                                        color = Brand.colors.accentStrong
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val HOUR_MS = 3_600_000L

/** Sample articles for previews and UI tests: the Figma frames' list. */
val ArticlesPreviewData: List<Article> = listOf(
    Article(
        id = "1",
        title = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
        publishDate = Date(System.currentTimeMillis() - 5 * HOUR_MS),
        content = "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى.",
    ),
    Article(
        id = "2",
        title = "فليصلّ النصارى من أجل السلام، ولكن…",
        publishDate = Date(System.currentTimeMillis() - 48 * HOUR_MS),
        content = "الحمد لله، والصلاة والسلام على رسول الله، أما بعد: فقد تابعنا ما يدور…",
    ),
    Article(
        id = "3",
        title = "مسائل في زكاة الفطر",
        publishDate = Date(System.currentTimeMillis() - 5 * 24 * HOUR_MS),
        content = "زكاة الفطر فريضة على كل مسلم، صغيرًا كان أو كبيرًا، ذكرًا أو أنثى.",
    ),
    Article(
        id = "4",
        title = "رايات نصر بلادنا تتعالى",
        publishDate = Date(System.currentTimeMillis() - 180 * 24 * HOUR_MS),
        content = "الحمد لله وحده، والصلاة والسلام على من لا نبي بعده…",
    ),
)

/**
 * [ArticlesPreviewData] as fully loaded paging data (plain `PagingData.from(list)` keeps
 * the refresh state Loading, which shows the spinner).
 */
fun articlesPreviewPagingData(): Flow<PagingData<Article>> = flowOf(
    PagingData.from(
        ArticlesPreviewData,
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)

@Composable
private fun ArticlesPreview(darkTheme: Boolean) {
    val articles = remember { articlesPreviewPagingData() }.collectAsLazyPagingItems()
    AdaptiveShellPreview(darkTheme = darkTheme) {
        ArticlesAdaptiveContent(
            articles = articles,
            selectedArticleId = null,
            onSelectArticle = {},
            onNavigateToArticleDetail = {},
            onNavigateBack = {},
            readerPane = { ArticleReaderPaneContent(ReaderPreviewState, fontStep = 1, onFontStepChange = {}, onShare = {}) },
        )
    }
}

@Preview(name = "Articles - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ArticlesCompactLightPreview() = ArticlesPreview(darkTheme = false)

@Preview(name = "Articles - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ArticlesCompactDarkPreview() = ArticlesPreview(darkTheme = true)

@Preview(name = "Articles - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ArticlesMediumLightPreview() = ArticlesPreview(darkTheme = false)

@Preview(name = "Articles - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ArticlesMediumDarkPreview() = ArticlesPreview(darkTheme = true)

@Preview(name = "Articles - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ArticlesExpandedLightPreview() = ArticlesPreview(darkTheme = false)

@Preview(name = "Articles - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ArticlesExpandedDarkPreview() = ArticlesPreview(darkTheme = true)
