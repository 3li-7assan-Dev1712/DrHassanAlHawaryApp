package com.example.feature.home.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.navigation.Routes
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
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
    val reduced = reducedMotion
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
            val categories = listOf(
                Category(
                    Routes.ARTICLES_SCREEN,
                    stringResource(R.string.articles),
                    TablerIcons.Notebook
                ),
                Category(
                    Routes.AUDIO_LIST_SCREEN,
                    stringResource(R.string.audios),
                    TablerIcons.Headphones
                ),
                Category(
                    Routes.VIDEOS_SCREEN,
                    stringResource(R.string.videos),
                    TablerIcons.Video
                ),
                Category(
                    Routes.Q_A_SCREEN,
                    stringResource(R.string.fasalo),
                    TablerIcons.MessageQuestion
                ),
                Category(
                    Routes.IMAGES_SCREEN,
                    stringResource(R.string.images),
                    TablerIcons.Photo
                ),
                Category(
                    Routes.ABOUT_DR_HASSAN_SCREEN,
                    stringResource(R.string.about_dr_hassan),
                    TablerIcons.UserCircle
                )
            )

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
                    LatestArticleAudioLazyRow(
                        modifier = Modifier.staggeredEntrance(3, entrance),
                        title = stringResource(R.string.latest_articles),
                        showLoading = uiState.loadingLatestArticles,
                        items = uiState.latestArticles,
                        emptyMessage = stringResource(R.string.no_articles_available),
                        onSeeAll = { onCategoryClick(Routes.ARTICLES_SCREEN) },
                        itemKey = { article -> article.id },
                        itemContent = { article ->
                            ArticleCard(
                                article = article,
                                onClick = { articleId ->
                                    onNavigateToDetailArticle(articleId)
                                },
                                // 85% of the row, so the next card peeks in.
                                modifier = Modifier.fillParentMaxWidth(0.85f),
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(SECTION_GAP))

                    AnimatedContent(
                        targetState = uiState.errorMessage,
                        transitionSpec = { Motion.contentSwap(reduced) },
                        contentKey = { it != null },
                        modifier = Modifier.staggeredEntrance(4, entrance),
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
                                itemContent = { audio ->
                                    AudioCard(
                                        modifier = Modifier.fillParentMaxWidth(0.75f),
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
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


private val SECTION_GAP = 20.dp

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "الشاشة الرئيسية")
@Composable
fun HomeScreenPreview() {
    val dummyArticles = listOf(
        ArticleFeed(
            id = "1",
            title = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
            excerpt = "هبوط فظيع في قيمة سعر الصرف مقابل العملات الأخرى، حتى وصلت أرقامًا فلكية يصعب حسابها.",
            publishedAt = System.currentTimeMillis() - 3 * 86_400_000L,
            readingMinutes = 7,
        ),
        ArticleFeed(id = "2", title = "فضل بر الوالدين", excerpt = "إن بر الوالدين من أعظم القربات إلى الله تعالى وأحبها إليه.", readingMinutes = 3)
    )
    val dummyAudios = listOf(
        AudioFeed(id = "1", title = "خطبة بعنوان: فضل العشر", audioUrl = "", duration = 1_499_000, displayTitle = "فضل العشر، والأضحية", hijriDate = HijriDate(27, "ذو القعدة", 1447)),
        AudioFeed(id = "2", title = "محاضرة - 6 ربيع الآخر 1448هـ", audioUrl = "", duration = 1_499_000, displayTitle = "محاضرة", hijriDate = HijriDate(6, "ربيع الآخر", 1448))
    )

    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                HomeScreenContent(
                    uiState = HomeScreenUiState(
                        latestArticles = dummyArticles,
                        latestAudios = dummyAudios,
                        loadingLatestArticles = false,
                        loadingLatestAudios = false,
                        loadingImages = false
                    ),
                    onNavigateToDetailArticle = {},
                    onNavigateToDetailAudio = { _, _ -> },
                    onCategoryClick = {}
                )
            }
        }
    }
}
