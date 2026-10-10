package com.example.feature.video.presentation.list

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.CategoryDef
import com.example.core.ui.components.ContentCategories
import com.example.core.ui.components.FilterPill
import com.example.core.ui.theme.layoutTokens
import com.example.feature.video.presentation.category.ALL_VIDEO_CATEGORIES_ID
import com.example.feature.video.presentation.category.VideoCategoryViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import com.example.core.ui.theme.Brand
import com.example.domain.module.Video
import com.example.feature.video.presentation.components.VideoCard


/**
 * The videos. On the phone, of the category chosen on the previous screen. On a tablet
 * (Figma Videos, no categories screen there) the categories are pills above the grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    viewModel: VideosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (String, String) -> Unit
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val categories = if (layoutTokens.isCompact) {
        emptyList()
    } else {
        val categoriesViewModel: VideoCategoryViewModel = hiltViewModel()
        val categoriesState by categoriesViewModel.uiState.collectAsStateWithLifecycle()
        // The ViewModel puts its own "الكل" first.
        remember(categoriesState.categories) {
            ContentCategories.resolve(categoriesState.categories.map { it.id to it.title })
        }
    }

    VideosScreenContent(
        videos = videos,
        categoryTitle = viewModel.categoryTitle,
        onNavigateBack = onNavigateBack,
        onNavigateToVideo = onNavigateToVideo,
        categories = categories,
        selectedCategoryId = categoryId ?: ALL_VIDEO_CATEGORIES_ID,
        onSelectCategory = { id -> viewModel.selectCategory(id.takeUnless { it == ALL_VIDEO_CATEGORIES_ID }) },
    )
}

/**
 * Compact: the phone's list of cards. Medium and Expanded: the category pills under the top
 * bar, then a grid of cards at their designed width, centred (Figma `60:1403`, `64:3744`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreenContent(
    videos: LazyPagingItems<Video>,
    categoryTitle: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (String, String) -> Unit,
    categories: List<CategoryDef> = emptyList(),
    selectedCategoryId: String = ALL_VIDEO_CATEGORIES_ID,
    onSelectCategory: (String) -> Unit = {},
) {
    val tokens = layoutTokens
    // "الكل" is a category on the previous screen, not a title: show "الفيديوهات" for it.
    // On a tablet the pills show the category, so always "الفيديوهات".
    val title = categoryTitle?.takeIf { tokens.isCompact && it.isNotBlank() && it != ALL_CATEGORIES_TITLE }
        ?: stringResource(id = R.string.videos)
    Scaffold(
        containerColor = Brand.colors.background,
        topBar = { AppTopBar(title = title, onBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!tokens.isCompact) {
                if (tokens.isExpanded) Spacer(Modifier.height(VideosTablet.BlockGap))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(categories, key = { it.id }) { category ->
                        FilterPill(
                            label = category.name,
                            selected = category.id == selectedCategoryId,
                            onClick = { onSelectCategory(category.id) },
                        )
                    }
                }
                if (tokens.isExpanded) Spacer(Modifier.height(VideosTablet.BlockGap))
            }
            Box(modifier = Modifier.fillMaxSize()) {
                val isMediatorRefreshing = videos.loadState.mediator?.refresh is LoadState.Loading

                val phase = when {
                    isMediatorRefreshing -> ContentPhase.Loading
                    videos.itemCount == 0 && videos.loadState.refresh is LoadState.NotLoading -> ContentPhase.Empty
                    else -> ContentPhase.Content
                }
                val reduced = reducedMotion

                AnimatedContent(
                    targetState = phase,
                    transitionSpec = { Motion.contentSwap(reduced) },
                    label = "videosContent",
                ) { shown ->
                    when (shown) {
                        ContentPhase.Loading -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                color = Brand.colors.accentStrong
                            )
                        }

                        ContentPhase.Empty, ContentPhase.Error -> Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Brand.colors.textMuted.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_videos_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Brand.colors.textMuted
                            )
                        }

                        ContentPhase.Content -> if (!tokens.isCompact) {
                            VideoGrid(videos = videos, onNavigateToVideo = onNavigateToVideo)
                        } else LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                count = videos.itemCount,
                                key = videos.itemKey { it.id }
                            ) { index ->
                                val video = videos[index]
                                if (video != null) {
                                    VideoCard(
                                        video = video,
                                        onVideoClick = {
                                            onNavigateToVideo(video.videoUrl, video.title)
                                        }
                                    )
                                }
                            }

                            item {
                                if (videos.loadState.append is LoadState.Loading) {
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
}

private const val ALL_CATEGORIES_TITLE = "الكل"

/** Tablet videos, measured in Figma (`60:1403` Expanded, `64:3744` Medium). */
private object VideosTablet {
    /** VideoCard's designed width: the cards keep it and the grid is centred. */
    val CardWidth = 328.dp
    val Gap = 24.dp
    /** Expanded: top bar to pills, pills to grid. */
    val BlockGap = 24.dp
}

/** As many [VideosTablet.CardWidth] columns as fit (3 on Expanded, 2 on Medium), centred, 24 apart. */
@Composable
private fun VideoGrid(videos: LazyPagingItems<Video>, onNavigateToVideo: (String, String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.FixedSize(VideosTablet.CardWidth),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = VideosTablet.Gap),
        horizontalArrangement = Arrangement.spacedBy(VideosTablet.Gap, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(VideosTablet.Gap),
    ) {
        items(count = videos.itemCount, key = videos.itemKey { it.id }) { index ->
            val video = videos[index]
            if (video != null) {
                VideoCard(video = video, onVideoClick = { onNavigateToVideo(video.videoUrl, video.title) })
            }
        }
        if (videos.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
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

/** Sample videos for previews and UI tests (the Figma frames' titles). */
val VideosPreviewData: List<Video> = List(6) { index ->
    Video(
        id = "$index",
        title = if (index % 2 == 0) "ما تيسر من سورة يس" else "هل من يموت في المظاهرات شهيد؟",
        videoUrl = "video-$index",
        categoryId = if (index % 2 == 0) "telawat" else "fatawah",
        publishDate = Date(System.currentTimeMillis() - 30L * 24 * 3_600_000L),
        youtubeVideoId = null,
    )
}

/** [VideosPreviewData] as fully loaded paging data. */
fun videosPreviewPagingData(): Flow<PagingData<Video>> = flowOf(
    PagingData.from(
        VideosPreviewData,
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)

@Composable
private fun VideosPreview(darkTheme: Boolean) {
    val videos = remember { videosPreviewPagingData() }.collectAsLazyPagingItems()
    AdaptiveShellPreview(darkTheme = darkTheme, mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin) {
        VideosScreenContent(
            videos = videos,
            onNavigateBack = {},
            onNavigateToVideo = { _, _ -> },
            categories = ContentCategories.all,
        )
    }
}

@Preview(name = "Videos - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun VideosCompactLightPreview() = VideosPreview(darkTheme = false)

@Preview(name = "Videos - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun VideosCompactDarkPreview() = VideosPreview(darkTheme = true)

@Preview(name = "Videos - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun VideosMediumLightPreview() = VideosPreview(darkTheme = false)

@Preview(name = "Videos - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun VideosMediumDarkPreview() = VideosPreview(darkTheme = true)

@Preview(name = "Videos - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun VideosExpandedLightPreview() = VideosPreview(darkTheme = false)

@Preview(name = "Videos - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun VideosExpandedDarkPreview() = VideosPreview(darkTheme = true)
