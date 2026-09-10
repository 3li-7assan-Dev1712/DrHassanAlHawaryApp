package com.example.feature.home.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Brush
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
import com.example.core.ui.theme.BrandTheme
import com.example.core.ui.theme.CairoTypography
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.theme.LocalBrandTheme
import com.example.domain.module.NetworkMessageEvent
import com.example.feature.home.R
import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.presentation.components.ArticleCard
import com.example.feature.home.presentation.components.AudioCard
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
    onCategoryClick: (route: String) -> Unit = {}
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
        onCategoryClick = onCategoryClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeScreenUiState,
    onNavigateToDetailArticle: (articleId: String) -> Unit,
    onNavigateToDetailAudio: (title: String, audioUrl: String) -> Unit,
    onCategoryClick: (route: String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(CoreR.string.app_name),
                    style = CairoTypography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { contentPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(contentPadding)
        ) {
            val isGreen = LocalBrandTheme.current == BrandTheme.GREEN
            val categories = listOf(
                Category(
                    Routes.ARTICLES_SCREEN,
                    stringResource(R.string.articles),
                    if (isGreen) CoreR.drawable.articles_icon_green else CoreR.drawable.articles_icon
                ),
                Category(
                    Routes.AUDIO_LIST_SCREEN,
                    stringResource(R.string.audios),
                    if (isGreen) CoreR.drawable.audios_icon_green else CoreR.drawable.audios_icon
                ),
                Category(
                    Routes.VIDEOS_SCREEN,
                    stringResource(R.string.videos),
                    if (isGreen) CoreR.drawable.videos_icon_green else CoreR.drawable.videos_icon
                ),
                Category(
                    Routes.Q_A_SCREEN,
                    stringResource(R.string.fasalo),
                    if (isGreen) R.drawable.fasalo_logo_green else R.drawable.fasalo_logo
                ),
                Category(
                    Routes.IMAGES_SCREEN,
                    stringResource(R.string.images),
                    if (isGreen) CoreR.drawable.images_icon_green else CoreR.drawable.images_icon
                ),
                Category(
                    Routes.ABOUT_DR_HASSAN_SCREEN,
                    stringResource(R.string.about_dr_hassan),
                    if (isGreen) CoreR.drawable.cv_icon_green else CoreR.drawable.cv_icon
                )
            )

            LazyColumn {
                item {
                    ImageCarousel(imageList = uiState.latestImages, isLoadingImages = uiState.loadingImages)
                }
                item {
                    LessonsByCategory(categories) { route ->
                        onCategoryClick(route)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    LatestArticleAudioLazyRow(
                        title = stringResource(R.string.latest_articles),
                        showLoading = uiState.loadingLatestArticles,
                        items = uiState.latestArticles,
                        emptyMessage = stringResource(R.string.no_articles_available),
                        itemKey = { article -> article.id },
                        itemContent = { article ->
                            ArticleCard(
                                article = article,
                                onClick = { articleId ->
                                    onNavigateToDetailArticle(articleId)
                                }
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        LatestArticleAudioLazyRow(
                            itemSpacing = 8.dp,
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp),
                            title = stringResource(R.string.latest_audios),
                            showLoading = uiState.loadingLatestAudios,
                            items = uiState.latestAudios,
                            emptyMessage = stringResource(R.string.no_audios_available),
                            itemKey = { audio -> audio.audioUrl },
                            itemContent = { audio ->
                                AudioCard(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .width(180.dp)
                                        .height(120.dp),
                                    audio = audio,
                                    onClick = {
                                        onNavigateToDetailAudio(audio.title, audio.audioUrl)
                                    }
                                )
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "الشاشة الرئيسية")
@Composable
fun HomeScreenPreview() {
    val dummyArticles = listOf(
        ArticleFeed(id = "1", title = "أهمية الصلاة في وقتها", contentPreview = "محتوى المقال هنا..."),
        ArticleFeed(id = "2", title = "فضل بر الوالدين", contentPreview = "محتوى المقال هنا...")
    )
    val dummyAudios = listOf(
        AudioFeed(id = "1", title = "تفسير سورة الفاتحة", audioUrl = "", duration = 300000),
        AudioFeed(id = "2", title = "شرح متن الآجرومية", audioUrl = "", duration = 600000)
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
