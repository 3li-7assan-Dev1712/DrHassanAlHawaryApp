package com.example.feature.home.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.navigation.Routes
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.layoutTokens
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import com.example.core.ui.theme.rememberFirstEntrance
import com.example.core.ui.theme.staggeredEntrance
import androidx.compose.animation.AnimatedContent
import com.example.core.ui.util.LightSystemBarIcons
import com.example.domain.module.NetworkMessageEvent
import com.example.domain.text.HijriDate
import com.example.feature.home.R
import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.presentation.components.ArticleCard
import com.example.feature.home.presentation.components.AudioCard
import com.example.feature.home.presentation.components.HomeHeader
import com.example.feature.home.presentation.components.Category
import com.example.feature.home.presentation.components.ImageCarousel
import com.example.feature.home.presentation.components.LatestArticleAudioLazyRow
import com.example.feature.home.presentation.components.LessonsByCategory
import com.example.core.ui.R as CoreR


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToDetailArticle: (articleId: String) -> Unit = {},
    onNavigateToDetailAudio: (title: String, audioUrl: String) -> Unit = { _, _ -> },
    onCategoryClick: (route: String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val homeScreenUiState by homeScreenViewModel.homeScreenUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val offlineMsg = stringResource(R.string.home_offline_message)
    val backOnlineMsg = stringResource(R.string.home_back_online_message)

    LaunchedEffect(key1 = Unit) {
        homeScreenViewModel.networkMessageEventFlow.collect { event ->
            when (event) {
                is NetworkMessageEvent.WentOffline -> {
                    Toast.makeText(context, offlineMsg, Toast.LENGTH_SHORT).show()
                }
                is NetworkMessageEvent.BackOnline -> {
                    Toast.makeText(context, backOnlineMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeScreenContent(
        modifier = modifier,
        uiState = homeScreenUiState,
        onNavigateToDetailArticle = onNavigateToDetailArticle,
        onNavigateToDetailAudio = onNavigateToDetailAudio,
        onCategoryClick = onCategoryClick,
        onNotificationsClick = onNotificationsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeScreenUiState,
    onNavigateToDetailArticle: (articleId: String) -> Unit,
    onNavigateToDetailAudio: (title: String, audioUrl: String) -> Unit,
    onCategoryClick: (route: String) -> Unit,
    onNotificationsClick: () -> Unit = {},
) {
    // Home follows the app's light/dark setting through Brand.colors; on the dark
    // palette the bars need light icons whatever the phone's own mode is.
    if (Brand.colors.isDark) LightSystemBarIcons()
    // First visit only: header, carousel, grid and the two rows fade in and rise, 40ms apart.
    val entrance = rememberFirstEntrance()
    Scaffold(
        containerColor = Brand.colors.background,
        // The host (MainActivity) already pads for the system bars; don't add them twice.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            HomeHeader(
                modifier = Modifier.staggeredEntrance(0, entrance),
                hasUnreadNotifications = uiState.unreadNotifications > 0,
                onNotificationsClick = onNotificationsClick,
            )
        }
    ) { contentPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brand.colors.background)
                .padding(contentPadding)
        ) {
            val categories = homeCategories()
            val tokens = layoutTokens
            if (tokens.isCompact) {
                LazyColumn {
                    item {
                        ImageCarousel(
                            modifier = Modifier.staggeredEntrance(1, entrance),
                            imageList = uiState.latestImages,
                            isLoadingImages = uiState.loadingImages,
                        )
                    }
                    item {
                        LessonsByCategory(
                            categories,
                            modifier = Modifier
                                .staggeredEntrance(2, entrance)
                                .padding(top = 12.dp),
                        ) { route ->
                            onCategoryClick(route)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(SECTION_GAP))
                        LatestArticlesSection(
                            uiState = uiState,
                            onNavigateToDetailArticle = onNavigateToDetailArticle,
                            onCategoryClick = onCategoryClick,
                            modifier = Modifier.staggeredEntrance(3, entrance),
                            // 85% of the row, so the next card peeks in.
                            cardWidth = { Modifier.fillParentMaxWidth(0.85f) },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(SECTION_GAP))
                        LatestAudiosSection(
                            uiState = uiState,
                            onNavigateToDetailAudio = onNavigateToDetailAudio,
                            onCategoryClick = onCategoryClick,
                            modifier = Modifier.staggeredEntrance(4, entrance),
                            cardWidth = { Modifier.fillParentMaxWidth(0.75f) },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Medium: one column, 12 between blocks (Figma 63:2756). Expanded: the header
                // over two equal columns 24 apart, the carousel and the tiles at the start,
                // the latest articles and audios at the end (Figma 56:192).
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    val featured: @Composable ColumnScope.() -> Unit = {
                        ImageCarousel(
                            modifier = Modifier.staggeredEntrance(1, entrance),
                            imageList = uiState.latestImages,
                            isLoadingImages = uiState.loadingImages,
                            horizontalPadding = 0.dp,
                        )
                        Spacer(Modifier.height(if (tokens.isExpanded) HomeTablet.CarouselToTiles else HomeTablet.BlockGap))
                        LessonsByCategory(
                            categories,
                            modifier = Modifier
                                .staggeredEntrance(2, entrance)
                                .padding(top = 12.dp),
                            horizontalPadding = 0.dp,
                            onCategoryClick = onCategoryClick,
                        )
                    }
                    val latest: @Composable ColumnScope.() -> Unit = {
                        TwoAcrossSections(
                            uiState = uiState,
                            onNavigateToDetailArticle = onNavigateToDetailArticle,
                            onNavigateToDetailAudio = onNavigateToDetailAudio,
                            onCategoryClick = onCategoryClick,
                            entrance = entrance,
                        )
                    }
                    if (tokens.isExpanded) {
                        Spacer(Modifier.height(HomeTablet.HeaderToFeed))
                        Row(horizontalArrangement = Arrangement.spacedBy(HomeTablet.ColumnGap)) {
                            Column(Modifier.weight(1f), content = featured)
                            Column(Modifier.weight(1f), content = latest)
                        }
                    } else {
                        Spacer(Modifier.height(HomeTablet.BlockGap))
                        featured()
                        Spacer(Modifier.height(HomeTablet.BlockGap))
                        latest()
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/** Tablet Home, measured in Figma (`56:192` Expanded, `63:2756` Medium). */
private object HomeTablet {
    val HeaderToFeed = 24.dp
    val ColumnGap = 24.dp
    val CarouselToTiles = 24.dp
    val BlockGap = 12.dp
    /** The section rows' start inset (the phone's), none at the end: two cards fill the rest. */
    val RowStartPadding = 16.dp
    val CardGap = 8.dp
}

/**
 * Medium and Expanded: the latest articles and audios, 12 between header, row and the next
 * header, each row showing two cards across (the rest scroll in).
 */
@Composable
private fun TwoAcrossSections(
    uiState: HomeScreenUiState,
    onNavigateToDetailArticle: (articleId: String) -> Unit,
    onNavigateToDetailAudio: (title: String, audioUrl: String) -> Unit,
    onCategoryClick: (route: String) -> Unit,
    entrance: Boolean,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cardWidth = (maxWidth - HomeTablet.RowStartPadding - HomeTablet.CardGap) / 2
        val rowPadding = PaddingValues(start = HomeTablet.RowStartPadding)
        Column(verticalArrangement = Arrangement.spacedBy(HomeTablet.BlockGap)) {
            LatestArticlesSection(
                uiState = uiState,
                onNavigateToDetailArticle = onNavigateToDetailArticle,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.staggeredEntrance(3, entrance),
                cardWidth = { Modifier.width(cardWidth) },
                contentPadding = rowPadding,
                headerSpacing = HomeTablet.BlockGap,
            )
            LatestAudiosSection(
                uiState = uiState,
                onNavigateToDetailAudio = onNavigateToDetailAudio,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.staggeredEntrance(4, entrance),
                cardWidth = { Modifier.width(cardWidth) },
                contentPadding = rowPadding,
                headerSpacing = HomeTablet.BlockGap,
            )
        }
    }
}

/** The six content shortcuts. */
@Composable
private fun homeCategories(): List<Category> = listOf(
    Category(Routes.ARTICLES_SCREEN, stringResource(R.string.articles), TablerIcons.Notebook),
    Category(Routes.AUDIO_LIST_SCREEN, stringResource(R.string.audios), TablerIcons.Headphones),
    Category(Routes.VIDEOS_SCREEN, stringResource(R.string.videos), TablerIcons.Video),
    Category(Routes.Q_A_SCREEN, stringResource(R.string.fasalo), TablerIcons.MessageQuestion),
    Category(Routes.IMAGES_SCREEN, stringResource(R.string.images), TablerIcons.Photo),
    Category(Routes.ABOUT_DR_HASSAN_SCREEN, stringResource(R.string.about_dr_hassan), TablerIcons.UserCircle),
)

@Composable
private fun LatestArticlesSection(
    uiState: HomeScreenUiState,
    onNavigateToDetailArticle: (articleId: String) -> Unit,
    onCategoryClick: (route: String) -> Unit,
    modifier: Modifier,
    cardWidth: LazyItemScope.() -> Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    headerSpacing: Dp = 0.dp,
) {
    LatestArticleAudioLazyRow(
        modifier = modifier,
        title = stringResource(R.string.latest_articles),
        showLoading = uiState.loadingLatestArticles,
        items = uiState.latestArticles,
        emptyMessage = stringResource(R.string.no_articles_available),
        onSeeAll = { onCategoryClick(Routes.ARTICLES_SCREEN) },
        itemKey = { article -> article.id },
        contentPadding = contentPadding,
        headerSpacing = headerSpacing,
        itemContent = { article ->
            ArticleCard(
                article = article,
                onClick = { articleId ->
                    onNavigateToDetailArticle(articleId)
                },
                modifier = cardWidth(),
            )
        }
    )
}

@Composable
private fun LatestAudiosSection(
    uiState: HomeScreenUiState,
    onNavigateToDetailAudio: (title: String, audioUrl: String) -> Unit,
    onCategoryClick: (route: String) -> Unit,
    modifier: Modifier,
    cardWidth: LazyItemScope.() -> Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    headerSpacing: Dp = 0.dp,
) {
    val reduced = reducedMotion
    AnimatedContent(
        targetState = uiState.errorMessage,
        transitionSpec = { Motion.contentSwap(reduced) },
        contentKey = { it != null },
        modifier = modifier,
        label = "latestAudios",
    ) { errorMessage ->
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LatestArticleAudioLazyRow(
                title = stringResource(R.string.latest_audios),
                onSeeAll = { onCategoryClick(Routes.AUDIO_LIST_SCREEN) },
                showLoading = uiState.loadingLatestAudios,
                items = uiState.latestAudios,
                emptyMessage = stringResource(R.string.no_audios_available),
                itemKey = { audio -> audio.audioUrl },
                contentPadding = contentPadding,
                headerSpacing = headerSpacing,
                itemContent = { audio ->
                    AudioCard(
                        modifier = cardWidth(),
                        audio = audio,
                        onClick = {
                            onNavigateToDetailAudio(audio.title, audio.audioUrl)
                        }
                    )
                }
            )
        }
    }
}

private val SECTION_GAP = 20.dp

/** Sample Home content for previews and UI tests (the Figma frames' cards). */
val HomePreviewState = HomeScreenUiState(
    latestArticles = listOf(
        ArticleFeed(
            id = "1",
            title = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
            excerpt = "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى وإليه الملاذ.",
            publishedAt = System.currentTimeMillis() - 3 * 86_400_000L,
            readingMinutes = 7,
        ),
        ArticleFeed(
            id = "2",
            title = "فضل بر الوالدين",
            excerpt = "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى وإليه الملاذ.",
            publishedAt = System.currentTimeMillis() - 3 * 86_400_000L,
            readingMinutes = 7,
        ),
    ),
    latestAudios = listOf(
        AudioFeed(id = "1", title = "خطبة بعنوان: فضل العشر", audioUrl = "1", duration = 1_499_000, displayTitle = "فضل العشر، والأضحية", hijriDate = HijriDate(27, "ذو القعدة", 1447)),
        AudioFeed(id = "2", title = "محاضرة - 27 ذو القعدة 1447هـ", audioUrl = "2", duration = 1_499_000, displayTitle = "محاضرة", hijriDate = HijriDate(27, "ذو القعدة", 1447)),
    ),
    loadingLatestArticles = false,
    loadingLatestAudios = false,
    loadingImages = false,
)

@Composable
private fun HomePreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme) {
        HomeScreenContent(
            uiState = HomePreviewState,
            onNavigateToDetailArticle = {},
            onNavigateToDetailAudio = { _, _ -> },
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Home - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun HomeCompactLightPreview() = HomePreview(darkTheme = false)

@Preview(name = "Home - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun HomeCompactDarkPreview() = HomePreview(darkTheme = true)

@Preview(name = "Home - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun HomeMediumLightPreview() = HomePreview(darkTheme = false)

@Preview(name = "Home - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun HomeMediumDarkPreview() = HomePreview(darkTheme = true)

@Preview(name = "Home - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun HomeExpandedLightPreview() = HomePreview(darkTheme = false)

@Preview(name = "Home - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun HomeExpandedDarkPreview() = HomePreview(darkTheme = true)
